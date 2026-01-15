public class moveVision extends Component {
  private Vector2 joy, slid;
  public Characterbody ch;
  private float speedJ = 20;
  private float x, y, camx, camy;
  public SpatialObject cam, myobj,chao;
  private float cammin = -80, cammax = 80;
  public UITextView t;
  void start() {
    myobj = myObject;
    ch = myobj.findComponent("Characterbody");
    cam = myObject.findChildObject("vision");
    t = WorldController.findObject("TextView").findComponent("textview");
    chao =WorldController.findObject("chao");
  }

  void repeat() {
    if (joy == null) joy = Input.registerAxis("joy").getValue();
    if (slid == null) slid = Input.registerAxis("slid").getValue();
    if (key("w") || key("s") || key("a") || key("d")) movekey();
    else move(joy.x * speedJ, joy.y * speedJ);
    t.setText(""+(int)(Math.abs(chao.globalPosition.y-myobj.globalPosition.y)));
    slidCam(slid.x, slid.y);
  } 

  private void movekey() {
    float x = 0, y = 0;
    if (key("w")) y = +1;
    if (key("s")) y = -1;
    if (key("a")) x = -1;
    if (key("d")) x = +1;
    move(x * speedJ, -y * speedJ);
  }

  private boolean key(String key) {
    if (Input.keyboard.isKeyPressed(key)) return true;
    return false;
  }

  private void move(float x, float y) {
    ch.setSpeed(-x, y);
  }

  private void slidCam(float x, float y) {
    camx += x;
    myobj.getRotation().selfLookTo(new Vector3(Math.sin(-camx), 0, Math.cos(-camx)));
    camy = Math.clamp(cammin, (camy += y), cammax);
    cam.getRotation().selfLookTo(new Vector3(0, Math.sin(-camy), Math.cos(-camy)));
  }

  /*
  float shakeAmount = .15f, times = 0;
  public boolean onoff = true;

  private void shake(float value) {
    if (!onoff) return;
    Vector3 mypos = obj.position.copy();
    if (onoff) {
      times += 0.01f;
      float offsetX = (float) Random.range(-shakeAmount, shakeAmount);
      float offsetZ = (float) Random.range(-shakeAmount, shakeAmount);
      obj.setPosition(mypos.x + offsetX, mypos.y, mypos.z + offsetZ);
      if (times > value) {
        obj.setPosition(mypos);
        onoff = false;
        times = 0;
      }
    }
  }*/
} // 0.329f