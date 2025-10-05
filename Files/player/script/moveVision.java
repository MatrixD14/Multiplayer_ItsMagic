public class moveVision extends Component {
  private Vector2 vision, visionMouse, joy;
  private Characterbody charac = null;
  private float speedJ = 3, speedS = 1f, camv, camh;
  public float x, y;

  void start() {
    charac = myObject.getPhysics().getPhysicsEntity();
    joy = Input.getAxisValue("joy");
    vision = Input.getAxisValue("vision");
  }

  void repeat() {
    visionMouse = new Vector2(Input.mouse.getSlideX(), Input.mouse.getSlideY());
    x = Input.mouse.getSlideX();
    y = Input.mouse.getSlideY();
    if (key("w") || key("s") || key("a") || key("d")) {
      movekey();
    } else move(joy.x * speedJ, joy.y * speedJ);

    /*    if (Input.mouse.isConnected() && !Input.getTouch(0).isPressed()) {
      vision(x < -20 && x > 20 ? 0 : x * speedS, y < -5 && y > 5 ? 0 : y * speedS);
    } else */
    vision(vision.x * speedS, vision.y * speedS);
  }

  private void movekey() {
    float x = 0, y = 0;
    if (key("w")) y = +1;
    if (key("s")) y = -1;
    if (key("a")) x = -1;
    if (key("d")) x = +1;
    move(x * speedJ, y * speedJ);
  } 

  private boolean key(String key) {
    if (Input.keyboard.isKeyPressed(key)) return true;
    return false;
  }

  private void move(float x, float y) {
    charac.setSpeed(-x, -y);
  }

  private void vision(float x, float y) {
    camh = Math.clamp(-80, camh += y, 45);
    myObject.findChildObject("vision").getRotation().selfLookTo(new Vector3(0, Math.sin(-camh), Math.cos(-camh)));
    camv += x;
    myObject.getRotation().selfLookTo(new Vector3(Math.sin(-camv), 0, Math.cos(-camv)));
  }
}
