package ypsilon.app.cdn;

interface CounterSvcIF {
	boolean setTime (int time, int pretime, boolean is_loop);
	void setloop (boolean is_loop);
	void start (int time, int pretime, boolean is_loop);
	void pause ();
	void restart ();
	void stop ();
	void end ();
	Bundle getState();
}
