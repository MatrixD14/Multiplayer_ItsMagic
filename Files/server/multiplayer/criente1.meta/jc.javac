import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class criente1 extends Component {
  private String host = "", msg = "", nome;
  private int port = 5000, maxPlayer = 10, myId = 0;
  Socket socket;
  private volatile boolean connected = false;
  private SpatialObject localPlayer;
  ObjectFile localplay;
  private int[] remoteId = new int[maxPlayer];
  private SpatialObject[] remotePlay = new SpatialObject[maxPlayer];

  private SUIText txts;
  private server1 checkServe;

  void start() {
    txts = WorldController.findObject("Ip").findComponent("suitext");
    checkServe = myObject.findComponent("server1");
  }

  void repeat() {
    if (Input.isKeyDown("serv") && !checkServe.running) {
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
      if (nome == null || nome.isEmpty()) {
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
              OutputStream out = socket.getOutputStream();
              out.write(("join:" + nome + "\n").getBytes("UTF-8"));
              out.flush();

              return "Conectado ao servidor";
            } catch (Exception e) {
              return "Erro conectar: " + e.getMessage();
            }
          }

          public void onEngine(Object result) {
            String msgResult = (String) result;
            Toast.showText(msgResult, 1);
            Console.log(msgResult);
            if (connected) {
              txts.setText("IP: " + socket.getInetAddress().getHostAddress());
              startListening();
            } 
          }
        });
  }

  private void startListening() {
    new AsyncTask(
        new AsyncRunnable() {
          public Object onBackground(Object input) {
            try {
              BufferedReader rend = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
              String line;

              while (connected && (line = rend.readLine()) != null) {
                //Thread.sleep(1);
                //return line;
                processServ(line);
              }
            } catch (Exception e) {
              return "Erro cliente: " + e.getMessage();
            }
            return null;
          }

          public void onEngine(Object result) {
            if (result != null && connected) {
              String message = (String) result;
              startListening();
            }
          }
        });
  }

  private void processServ(String txt) {
    if (txt.startsWith("id:")) {
      myId = Integer.parseInt(txt.substring(3));
      localPlayer = myObject.instantiate(localplay);
      localPlayer.setPosition(0, 1, 0);
      localPlayer.setName(nome);

      // Envio de posição inline
      new AsyncTask(
          new AsyncRunnable() {
            public Object onBackground(Object input) {
              try {
                while (connected) {
                  float x = localPlayer.getPosition().x;
                  float y = localPlayer.getPosition().y;
                  float z = localPlayer.getPosition().z;
                  String posMsg = "pos:" + myId + ":" + x + ":" + y + ":" + z;
                  OutputStream out = socket.getOutputStream();
                  out.write((posMsg + "\n").getBytes("UTF-8"));
                  out.flush();
                  Thread.sleep(50);
                }
              } catch (Exception e) {
                desconnect();
              }
              return null;
            }

            public void onEngine(Object result) {}
          });

    } else if (txt.startsWith("spaw:")) {
      handleSpawn(txt);
    } else if (txt.startsWith("pos:")) {
      handlePos(txt);
    } else {
      Toast.showText(txt, 1);
      Console.log(txt);
    }
  }

  private void handleSpawn(String txt) {
    String[] p = txt.split(":");
    int id = Integer.parseInt(p[1]);
    if (id == myId) return;

    int slot = -1;
    for (int i = 0; i < maxPlayer; i++) {
      if (remoteId[i] == 0) {
        slot = i;
        break;
      }
    }
    if (slot == -1) return;

    String[] posParts = new String[] {p[3], p[4], p[5]};
    float x = Float.parseFloat(posParts[0]);
    float y = Float.parseFloat(posParts[1]);
    float z = Float.parseFloat(posParts[2]);

    if (remotePlay[slot] == null) remotePlay[slot] = myObject.instantiate(localplay);
    remotePlay[slot].setPosition(x, y, z);
    remoteId[slot] = id;
  }

  private void handlePos(String txt) {
    String[] p = txt.split(":");
    int id = Integer.parseInt(p[1]);
    if (id == myId) return;
    int slot = -1;
    for (int i = 0; i < maxPlayer; i++) {
      if (remoteId[i] == id) {
        slot = i;
        break;
      }
    }
    if (slot == -1) return;
    float x = Float.parseFloat(p[2]);
    float y = Float.parseFloat(p[3]);
    float z = Float.parseFloat(p[4]);
    remotePlay[slot].setPosition(x, y, z);
  }

  void desconnect() {
    connected = false;
    try {
      if (socket != null && !socket.isClosed()) socket.close();
      nome = null;
    } catch (Exception e) {
      Toast.showText("Desconnect", 1);
    }
  }
}
