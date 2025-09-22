import java.net.Socket;

public class server extends Component {

  int port = 5000;
  ServerSocket server;
  volatile boolean running = false, checktrue = false;
  Vector<Socket> clients = new Vector<Socket>();

  public PropertiesButton start_server =
      new PropertiesButton(
          new PropertiesButtonListener() {
            void onClicked() {
              play();
            }
          });

  public PropertiesButton stop_server =
      new PropertiesButton(
          new PropertiesButtonListener() {
            void onClicked() {
              stop();
            }
          });

  void repeat() {
    if (Input.isKeyDown("serv")) {
      if (running) stop();
      else play();
    }
  }

  private void play() {
    if (running) {
      Toast.showText("Servidor já está rodando!", 1);
      return;
    }
    running = true;
    Console.log("Iniciando servidor na porta " + port);
    Toast.showText("Iniciando servidor na porta " + port, 1);

    new AsyncTask(
        new AsyncRunnable() {
          public Object onBackground(Object input) {
            try {
              server = new ServerSocket(port);
              while (running) {
                try {
                  final Socket client = server.accept();
                  clients.add(client);
                  Console.log("Cliente conectado: " + client.getInetAddress());
                  Toast.showText("Cliente conectado: " + client.getInetAddress(), 1);

                  // cada cliente é tratado em uma tarefa separada
                  new AsyncTask(
                      new AsyncRunnable() {
                        public Object onBackground(Object input) {
                          handleClient(client);
                          return null;
                        }

                        public void onEngine(Object result) {}
                      });

                } catch (SocketException se) {
                  // ServerSocket foi fechado (stop chamado)
                  break;
                } catch (IOException ioe) {
                  Console.log("Accept erro: " + ioe.getMessage());
                  Toast.showText("Accept erro: " + ioe.getMessage(), 1);
                }
              }
            } catch (Exception e) {
              Console.log("Erro servidor: " + e.getMessage());
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
      Console.log("Erro ao fechar ServerSocket: " + e.getMessage());
    }

    // fecha todos os clientes conectados
    for (int i = clients.size() - 1; i >= 0; i--) {
      Socket s = clients.get(i);
      try {
        s.close();
      } catch (Exception e) {
      }
      clients.remove(i);
    }

    Console.log("Servidor parado e clientes desconectados.");
    Toast.showText("Servidor parado!", 1);
  }

  private void handleClient(Socket client) {
    InputStream in = null;
    try {
      in = client.getInputStream();
      byte[] buffer = new byte[1024];
      StringBuilder sb = new StringBuilder();
      int read;

      while (running && !client.isClosed() && (read = in.read(buffer)) != -1) {
        sb.append(new String(buffer, 0, read, "UTF-8"));
        int idx;
        // processa todas as mensagens completas terminadas em '\n'
        while ((idx = sb.indexOf("\n")) != -1) {
          String msg = sb.substring(0, idx);
          sb.delete(0, idx + 1);

          Console.log("Recebido de " + client.getInetAddress() + ": " + msg);
          // reenvia para os outros clientes (não envia de volta para o remetente)
          broadcast(msg, client);
        }
      }
    } catch (Exception e) {
      Console.log("Erro client: " + e.getMessage());
    } finally {
      // remove e fecha
      try {
        client.close();
      } catch (Exception e) {
      }
      clients.remove(client);
      Console.log("Cliente desconectado: " + client.getInetAddress());
    }
  }

  // reenvia msg para todos exceto sender (se sender == null envia para todos)
  private void broadcast(String msg, Socket sender) {
    //String full = (sender == null ? "" : sender.getInetAddress().toString() + ": ") + msg + "\n";
    String full = msg + "\n";
    byte[] data;
    try {
      data = full.getBytes("UTF-8");
    } catch (Exception e) {
      data = full.getBytes();
    }

    // itera sobre a lista (Vector é sincronizado)
    for (int i = 0; i < clients.size(); i++) {
      Socket s = clients.get(i);
      if (s == null) continue;
      if (s.isClosed()) {
        clients.remove(i);
        i--;
        continue;
      }
      // não manda de volta para o remetente
      if (sender != null && s.equals(sender)) continue;

      try {
        OutputStream out = s.getOutputStream();
        out.write(data);
        out.flush();
      } catch (Exception e) {
        // cliente com problema — remove
        try {
          s.close();
        } catch (Exception ex) {
        }
        clients.remove(i);
        i--;
      }
    }
  }
}