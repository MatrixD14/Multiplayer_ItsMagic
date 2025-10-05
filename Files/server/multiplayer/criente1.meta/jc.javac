import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class criente1 extends Component {
  private String host = "", msg = "", nome;
  private int port = 5000, maxPlayer = 10, myId = 0;
  Socket socket;
  private volatile boolean connected = false;
  public SpatialObject localPlayer;
  public ObjectFile localplay, amigo;
  private int[] remoteId;
  private String[] remoteName;
  private SpatialObject[] remotePlay;
  float[][] posCache, rotCache;
  private Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();

  private SUIText txts;
  private server1 checkServe;

  void start() {
    if (maxPlayer <= 0) maxPlayer = 10;
    remoteId = new int[maxPlayer];
    remoteName = new String[maxPlayer];
    remotePlay = new SpatialObject[maxPlayer];
    posCache = new float[maxPlayer][3];
    rotCache = new float[maxPlayer][3];
    txts = WorldController.findObject("Ip").findComponent("suitext");
    checkServe = myObject.findComponent("server1");
  } 

  void repeat() {
    Runnable r;
    while ((r = queue.poll()) != null) r.run();
    for (int i = 0; i < maxPlayer; i++) {
      if (remotePlay[i] != null && remoteId[i] != 0) {
        remotePlay[i].setPosition(posCache[i][0], posCache[i][1], posCache[i][2]);
        remotePlay[i].setRotation(rotCache[i][0], rotCache[i][1], rotCache[i][2]);
      }
    }
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

  private void runOnMain(Runnable r) {
    queue.add(r);
  }

  private void startListening() {
    new AsyncTask(
        new AsyncRunnable() {
          public Object onBackground(Object input) {
            try {
              BufferedReader rend = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
              String line;

              while (connected && (line = rend.readLine()) != null) {
                processServ(line);
              }
            } catch (Exception e) {
              return "Erro cliente: " + e.getMessage();
            }
            return null;
          }

          public void onEngine(Object result) {
            if (result != null && connected) Console.log(result.toString());
          }
        });
  }

  private void processServ(String txt) {
    if (txt.startsWith("id:")) {
      myId = Integer.parseInt(txt.substring(3));
      runOnMain(
          () -> {
            if (localPlayer == null) {
              localPlayer = myObject.instantiate(localplay);
              localPlayer.setPosition(0, 1, 0);
              localPlayer.setName(nome);
            }
            new AsyncTask(
                new AsyncRunnable() {
                  public Object onBackground(Object input) {
                    try {
                      while (connected && socket != null && !socket.isClosed()) {
                        Vector3 pos = localPlayer.getPosition();
                        Quaternion rot = localPlayer.getRotation();
                        String posMsg = "pos:" + myId + ":" + pos.x + ":" + pos.y + ":" + pos.z;
                        String rotMsg = "rot:" + myId + ":" + rot.x + ":" + rot.y + ":" + rot.z;
                        OutputStream out = socket.getOutputStream();
                        out.write((posMsg + "\n").getBytes("UTF-8"));
                        out.write((rotMsg + "\n").getBytes("UTF-8"));
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
          });

    } else if (txt.startsWith("spaw:")) {
      handleSpawn(txt);
    } else if (txt.startsWith("pos:")) {
      handlePos(txt);
    }else if (txt.startsWith("rot:")) {
      handleRot(txt);
    } else if (txt.startsWith("left:")) {
      handleLeft(txt);
    } else {
      Toast.showText(txt, 1);
      Console.log(txt);
    }
  }

  private void handleSpawn(String txt) {
    String[] p = txt.split(":");
    final int id = Integer.parseInt(p[1]);
    if (id == myId) return;

    int slot = -1;
    for (int i = 0; i < maxPlayer; i++) {
      if (remoteId[i] == 0) {
        slot = i;
        break;
      }
    }
    if (slot == -1) return;
    final int tmpslot = slot;
    final String nomplayer = p[2];
    final float x = Float.parseFloat(p[3]);
    final float y = Float.parseFloat(p[4]);
    final float z = Float.parseFloat(p[5]);
    runOnMain(
        () -> {
          if (remotePlay[tmpslot] == null) {
            remotePlay[tmpslot] = myObject.instantiate(amigo);
            remotePlay[tmpslot].setName(nomplayer);
          }
          remotePlay[tmpslot].setPosition(x, y, z);
          remoteId[tmpslot] = id;
          remoteName[tmpslot] = nomplayer;
          Toast.showText(nomplayer + " entrou", 1);
        });
  }

  private void handlePos(String txt) {
    String[] p = txt.split(":");
    int id = Integer.parseInt(p[1]);
    if (id == myId) return;
    float x = Float.parseFloat(p[2]);
    float y = Float.parseFloat(p[3]);
    float z = Float.parseFloat(p[4]);

    for (int i = 0; i < maxPlayer; i++) {
      if (remoteId[i] == id) {
        posCache[i][0] = x;
        posCache[i][1] = y;
        posCache[i][2] = z;
        break;
      }
    }
  }
  private void handleRot(String txt){
     String[] p = txt.split(":");
    int id = Integer.parseInt(p[1]);
    if (id == myId) return;
    float x = Float.parseFloat(p[2]);
    float y = Float.parseFloat(p[3]);
    float z = Float.parseFloat(p[4]);

    for (int i = 0; i < maxPlayer; i++) {
      if (remoteId[i] == id) {
        rotCache[i][0] = x;
        rotCache[i][1] = y;
        rotCache[i][2] = z;
        break;
      }
    }
  }
  private void handleLeft(String txt) {
    String[] p = txt.split(":");
    final int id = Integer.parseInt(p[1]);
    runOnMain(
        () -> {
          for (int is = 0; is < maxPlayer; is++) {
            if (remoteId[is] == id) {
              if (remotePlay[is] != null) {
                remotePlay[is].destroy();
              }
              Toast.showText(remoteName[is] + " saiu!", 1);
              remotePlay[is] = null;
              remoteId[is] = 0;
              remoteName[is] = null;
              break;
            }
          }
        });
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
