import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class criente extends Component {
  String host = "", msg = "", nome;
  int port = 5000;
  Socket socket;
  volatile boolean connected = false, mssg = false;
  private SUIText txt, txts, mymsg;
  private List<String> history = new ArrayList<String>();
  private server checkServe;

  public PropertiesButton connectBtn =
      new PropertiesButton(
          new PropertiesButtonListener() {
            void onClicked() {
              connect();
            }
          });

  public PropertiesButton sendBtn =
      new PropertiesButton(
          new PropertiesButtonListener() {
            void onClicked() {
              send(msg);
            }
          });

  void start() {
    txt = WorldController.findObject("msgs").findComponent("suitext");
    txts = WorldController.findObject("Ip").findComponent("suitext");
    mymsg = WorldController.findObject("mymsg").findComponent("suitext");
    checkServe = myObject.findComponent("server");
  } 

  void repeat() {
    if (Input.isKeyDown("serv") && checkServe.running) {
      InputDialog inputN =
          new InputDialog(
              "nome usuario",
              "",
              "exit",
              "ok",
              new InputDialogListener() {
                public void onFinish(String t) {
                  nome = t;
                  host = "localhost";
                  connect();
                  txts.setText("IP: "+socket.getLocalAddress().getHostAddress());
                }

                public void onCancel() {}
              });
    }
    if (Input.isKeyDown("IP")) {
      InputDialog inputV =
          new InputDialog(
              "connect ao servidor",
              "",
              "sair",
              "connect",
              new InputDialogListener() {
                public void onFinish(String t) {
                  host = t;
                  txts.setText("IP: " + t);
                  connect();
                }

                public void onCancel() {}
              });
      if (!nome.isEmpty() || nome == "") {
        InputDialog inputN =
            new InputDialog(
                "nome usuario",
                "",
                "exit",
                "ok",
                new InputDialogListener() {
                  public void onFinish(String t) {
                    nome = t;
                    txts.setText("nome: " + t);
                  }

                  public void onCancel() {}
                });
      }
    }
    if (!connected && Input.isKeyDown("msg")) {
      Toast.showText("não connect", 1);
      return;
    }
    if (Input.isKeyDown("msg")) mssg = !mssg;
    if (mssg) Input.addKeyboardRequest(this);
    else Input.removeKeyboardRequest(this);

    for (KeyboardButton key : Input.keyboard.getDownButtons()) {
      String name = key.getName();
      if (name.equals("del")) msg = "";
      else if (!name.equals("enter")) msg += name;
    }

    mymsg.setText(msg);

    if (Input.keyboard.isKeyDown("enter") && !msg.isEmpty()) {
      send(msg);
      msg = "";
      mssg = false;
    }
  }

  private void addMessage(String message) {
    history.add(message);
    if (history.size() > 5) history.remove(0);
    StringBuilder sb = new StringBuilder();
    for (String s : history) sb.append(s).append("\n");
    txt.setText(sb.toString());
  }

  void connect() {
    if (connected) {
      Toast.showText("Já conectado", 1);
      return;
    }

    new AsyncTask(
        new AsyncRunnable() {
          public Object onBackground(Object input) {
            try {
              socket = new Socket(host, port);
              connected = true;

              // notifica conexão na engine
              return "Conectado ao servidor";

              // loop de leitura ficará em outro AsyncTask
            } catch (Exception e) {
              return "Erro conectar: " + e.getMessage();
            }
          }

          public void onEngine(Object result) {
            String msgResult = (String) result;
            Toast.showText(msgResult, 1);
            Console.log(msgResult);

            // se conectado, iniciar escuta do servidor
            if (connected) startListening();
          }
        });
  }

  private void startListening() {
    new AsyncTask(
        new AsyncRunnable() {
          public Object onBackground(Object input) {
            try {
              InputStream in = socket.getInputStream();
              byte[] buffer = new byte[1024];
              StringBuilder sb = new StringBuilder();
              int read;

              while (connected && (read = in.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, read, "UTF-8"));
                int idx;
                while ((idx = sb.indexOf("\n")) != -1) {
                  String msg = sb.substring(0, idx);
                  sb.delete(0, idx + 1);
                  // retorna cada mensagem para onEngine
                  Thread.sleep(1); // evita flood
                  return msg;
                }
              }
            } catch (Exception e) {
              return "Erro cliente: " + e.getMessage();
            }
            return null;
          }

          public void onEngine(Object result) {
            if (result != null && connected) {
              String message = (String) result;
              addMessage(message);
              // chama novamente para continuar escutando
              startListening();
            }
          }
        });
  }

  void send(String texts) {
    final String text = nome + ": " + texts;
    if (!connected || socket == null || socket.isClosed()) {
      Toast.showText("Não conectado", 1);
      return;
    }

    new AsyncTask(
        new AsyncRunnable() {
          public Object onBackground(Object input) {
            try {
              OutputStream out = socket.getOutputStream();
              out.write((text + "\n").getBytes("UTF-8"));
              out.flush();
            } catch (Exception e) {
              connected = false;
              return "Erro enviar: " + e.getMessage();
            }
            return null;
          }

          public void onEngine(Object result) {
            if (result != null) {
              Toast.showText((String) result, 1);
              Console.log((String) result);
            }
          }
        });
  }
}