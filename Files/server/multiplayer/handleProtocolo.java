public class handleProtocolo {
  public void handleRot(String txt, int myId, Vector3Buffer rotBufferCache, int[] remoteId, int maxPlayer) {
    String[] p = txt.split(":");
    int id = Integer.parseInt(p[1]);
    if (id == myId) return;
    float x = Float.parseFloat(p[2]);
    float y = Float.parseFloat(p[3]);
    float z = Float.parseFloat(p[4]);

    for (int i = 0; i < maxPlayer; i++) {
      if (remoteId[i] == id) {
        rotBufferCache.set(i, x, y, z);
        break;
      }
    }
  }

  public void handlePos(String txt, int myId, Vector3Buffer posBufferCache, int[] remoteId, int maxPlayer) {
    String[] p = txt.split(":");
    int id = Integer.parseInt(p[1]);
    if (id == myId) return;
    float x = Float.parseFloat(p[2]);
    float y = Float.parseFloat(p[3]);
    float z = Float.parseFloat(p[4]);

    for (int i = 0; i < maxPlayer; i++) {
      if (remoteId[i] == id) {
        posBufferCache.set(i, x, y, z);
        break;
      }
    }
  }
} 
