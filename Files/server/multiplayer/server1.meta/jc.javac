import java.net.Socket;

public class server1 extends Component {
  private int port = 5000, maxPlayer = 10;
  ServerSocket server;
  @Hide public volatile boolean running = false, checktrue = false;
  private Socket[] clients = new Socket[maxPlayer];
  private int[] clientId = new int[maxPlayer];
  private String[] clientName = new String[maxPlayer];
//  private int geraSeed;

  void repeat() {
    if (Input.isKeyDown("serv")) {
      if (running) stop();
      else play();
     // geraSeed = Random.range(100, 999);
    }
  }

  private void play() {
    if (running) {
      Toast.showText("Servidor já está rodando!", 1);
      return;
    }
    running = true;
    Terminal.log("Iniciando servidor na porta " + port);
    Toast.showText("Iniciando servidor na porta " + port, 1);

    new AsyncTask(
        new AsyncRunnable() {
          public Object onBackground(Object input) {
            try {
              server = new ServerSocket(port);
              while (running) {
                try {
                  final Socket client = server.accept();
                  final int slot = getFreeSlot();
                  if (slot == -1) {
                    sendClient(client, "full");
                    client.close();
                    continue;
                  }
                  clients[slot] = client;
                  clientId[slot] = slot + 1;
                  Terminal.log("Cliente conectado: " + client.getInetAddress());
                  Toast.showText("Cliente conectado: " + client.getInetAddress(), 1);

                  new AsyncTask(
                      new AsyncRunnable() {
                        public Object onBackground(Object input) {
                          handleClient(slot);
                          return null;
                        }

                        public void onEngine(Object result) {}
                      });

                } catch (SocketException se) {
                  break;
                } catch (IOException ioe) {
                  Terminal.log("Accept erro: " + ioe.getMessage());
                  Toast.showText("Accept erro: " + ioe.getMessage(), 1);
                }
              }
            } catch (Exception e) {
              Terminal.log("Erro servidor: " + e.getMessage());
              Toast.showText("Erro servidor: " + e.getMessage(), 1);
            }
            return null;
          }

          public void onEngine(Object result) {}
        });
  }

  void stop() {
    if (!running) {
      Toast.showText("Servidor já está parado!", 1);
      return;
    }
    running = false;
    try {
      if (server != null && !server.isClosed()) server.close();
    } catch (IOException e) {
      Terminal.log("Erro ao fechar ServerSocket: " + e.getMessage());
    }

    for (int i = 0; i < maxPlayer; i++) {
      if (clients[i] != null) {
        try {
          clients[i].close();
        } catch (Exception e) {
        }
        clients[i] = null;
        clientId[i] = 0;
        clientName[i] = null;
      }
    }
    Terminal.log("Servidor parado e clientes desconectados.");
    Toast.showText("Servidor parado!", 1);
  }

  private int getFreeSlot() {
    for (int i = 0; i < maxPlayer; i++) if (clients[i] == null) return i;
    return -1;
  }

  private void handleClient(int slot) {
    Socket client = clients[slot];
    if (client == null) return;
    try {
      BufferedReader rend = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
      String line;
      StringBuilder sbId = new StringBuilder(64), exists = new StringBuilder(64), spaw = new StringBuilder(64);
      while (running && !client.isClosed() && (line = rend.readLine()) != null) {
        if (line.startsWith("join:")) {
          sbId.setLength(0);
          String nome = line.substring(5);
          clientName[slot] = nome;
          sbId.append("id:").append(clientId[slot]);
          sendClient(client, sbId.toString());
         // spaw.setLength(0);
         // spaw.append("seed:0:").append(geraSeed);
        //  sendClient(client, spaw.toString());
          for (int i = 0; i < maxPlayer; i++) {
            if (clients[i] != null && i != slot && clientId[i] != 0 && clientName[i] != null) {
              exists.setLength(0);
              exists.append("spaw:").append(clientId[i]).append(":").append(clientName[i]).append(":0:1:0");
              sendClient(client, exists.toString());
            }
          } 
          /*spaw.setLength(0);
          spaw.append("seed:0:").append(geraSeed);
          broadcast(spaw.toString(), null);*/
          spaw.setLength(0);
          spaw.append("spaw:").append(clientId[slot]).append(":").append(nome).append(":0:1:0");
          broadcast(spaw.toString(), client);       
        } else if (line.startsWith("pos:") || line.startsWith("rot:")) broadcast(line, client);
        else broadcast(line, client);
      }
    } catch (Exception e) {
      Terminal.log("Erro client: " + e.getMessage());
    } finally {
      if (clientId[slot] != 0) broadcast("left:" + clientId[slot], null);
      try {
        clients[slot].close();
      } catch (Exception e) {
      }
      clients[slot] = null;
      clientId[slot] = 0;
      clientName[slot] = null;
      Terminal.log("Cliente desconectado: " + slot);
    }
  }

  private synchronized void broadcast(String msg, Socket sender) {
    StringBuilder full = new StringBuilder(msg.length() + 1);
    full.append(msg).append("\n");
    byte[] data;
    try {
      data = full.toString().getBytes("UTF-8");
    } catch (Exception e) {
      data = full.toString().getBytes();
    }
    for (int i = 0; i < maxPlayer; i++) {
      Socket s = clients[i];
      if (s == null || s.isClosed()) {
        clients[i] = null;
        clientId[i] = 0;
        clientName[i] = null;
        continue;
      }
      if (sender != null && s.equals(sender)) continue;

      try {
        OutputStream out = s.getOutputStream();
        out.write(data);
        out.flush();
      } catch (Exception e) {
        Terminal.log("Erro broadcast: " + e.getMessage());
        try {
          s.close();
        } catch (Exception es) {
        }
        clients[i] = null;
        clientId[i] = 0;
        clientName[i] = null;
      }
    }
  }

  private void sendClient(Socket s, String msg) {
    try {
      StringBuilder sb = new StringBuilder(msg.length() + 1);
      sb.append(msg).append("\n");
      OutputStream out = s.getOutputStream();
      out.write(sb.toString().getBytes("UTF-8"));
      out.flush();
    } catch (Exception e) {
    }
  }
}