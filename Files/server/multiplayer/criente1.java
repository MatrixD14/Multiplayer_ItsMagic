import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class criente1 extends Component {
  private String host = "", msg = "", nome;
  private int port = 5000, maxPlayer = 10, myId = 0;
  private Socket socket;
  private volatile boolean connected = false;
  private SpatialObject localPlayer;
  public ObjectFile localplay, amigo;

  private int[] remoteId;
  private String[] remoteName;
  private SpatialObject[] remotePlay;
  private Vector3Buffer posCache, rotCache, posBufferCache, rotBufferCache;
  private Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();

  private UITextView txt;
  private server1 checkServe;
  private handleProtocolo protocolo = new handleProtocolo();
 // private dangeonGeration seedgera;

  void start() {
    if (maxPlayer <= 0) maxPlayer = 10;
    remoteId = new int[maxPlayer];
    remoteName = new String[maxPlayer];
    remotePlay = new SpatialObject[maxPlayer];
    posCache = BufferUtils.createVector3Buffer(maxPlayer);
    rotCache = BufferUtils.createVector3Buffer(maxPlayer);
    posBufferCache = BufferUtils.createVector3Buffer(maxPlayer);
    rotBufferCache = BufferUtils.createVector3Buffer(maxPlayer);
    txt = WorldController.findObject("Ip").findComponent("TextView");
    checkServe = myObject.findComponent("server1");
    //seedgera = WorldController.findObject("dangeon").findComponent("dangeonGeration");
  }

  void repeat() {
    Runnable r;
    while ((r = queue.poll()) != null) r.run();
    for (int i = 0; i < maxPlayer; i++) {
      if (remotePlay[i] != null && remoteId[i] != 0) {
        float px = posCache.getX(i), py = posCache.getY(i), pz = posCache.getZ(i);
        float rx = rotCache.getX(i), ry = rotCache.getY(i), rz = rotCache.getZ(i);
        remotePlay[i].setPosition(px, py, pz);
        remotePlay[i].setRotation(rx, ry, rz);
      }
    }
    swap();
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
                  txt.setText("IP: " + t);
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
                    txt.setText("nome: " + t);
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
            Terminal.log(msgResult);
            if (connected) {
              txt.setText("IP: " + socket.getInetAddress().getHostAddress());
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
            if (result != null && connected) Terminal.log(result.toString());
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
                      StringBuilder sb = new StringBuilder();
                      OutputStream out = socket.getOutputStream();
                      while (connected && socket != null && !socket.isClosed()) {
                        sb.setLength(0);
                        Vector3 pos = localPlayer.getPosition();
                        Quaternion rot = localPlayer.getRotation();
                        sb.append("pos:").append(myId).append(":").append(pos.x).append(":").append(pos.y).append(":").append(pos.z).append("\n");
                        sb.append("rot:").append(myId).append(":").append(rot.x).append(":").append(rot.y).append(":").append(rot.z).append("\n");
                        out.write(sb.toString().getBytes("UTF-8"));
                        out.flush();
                        Thread.sleep(10);
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
    /*} else if (txt.startsWith("seed:")) {
      handleSeed(txt);*/
    } else if (txt.startsWith("pos:")) {
      protocolo.handlePos(txt, myId, posCache, remoteId, maxPlayer);
    } else if (txt.startsWith("rot:")) {
      protocolo.handleRot(txt, myId, rotCache, remoteId, maxPlayer);
    } else if (txt.startsWith("left:")) {
      handleLeft(txt);
    } else {
      Toast.showText(txt, 1);
      Terminal.log(txt);
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

/* public void handleSeed(String txt) {
    final String[] p = txt.split(":");
    int id = Integer.parseInt(p[1]);
    if (id == myId) return;
    runOnMain(
        () -> {
          seedgera.setSeed(Integer.parseInt(p[2]));
          seedgera.armGerador();
        });
  }*/

  void desconnect() {
    connected = false;
    try {
      if (socket != null && !socket.isClosed()) socket.close();
      nome = null;
    } catch (Exception e) {
      Toast.showText("Desconnect", 1);
    }
  }

  private void swap() {
    Vector3Buffer tmpPos = posCache, tmpRot = rotCache;
    posCache = posBufferCache;
    posBufferCache = tmpPos;
    rotCache = rotBufferCache;
    rotBufferCache = tmpRot;
  }
}