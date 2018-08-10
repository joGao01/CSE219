package dataprocessors;


public class Drop {
    private boolean pressed;

    public Drop(boolean pressed){
        this.pressed = pressed;
    }

//    public boolean getPressed(){ return pressed; }
    public void setPressed(boolean b){ pressed = b;}

    public synchronized void take(){
        while(pressed) {
            try {
             //   System.out.println("pressed?: " + pressed);
                wait();
            } catch (InterruptedException e) {
                //something
            }
        }
        pressed = true;
        notifyAll();
    }

    public synchronized void put(){
        while(!pressed){
            try{
                wait();
            } catch (InterruptedException e){
                //sometihng
            }
        }
        pressed = false;
        notifyAll();
    }

}
