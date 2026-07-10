import java.io.OutputStream;
import java.io.PrintStream;

import lejos.nxt.*;
import lejos.util.Stopwatch;

/**
 * EasyRobotLibrary.
 *
 * @author Dr. David (TM), Tomáš, Čeněk.
 * @version 2026.3
 */
public class Robotabor {

	/* Jmena tlacitek */

	public static final int ENTER = Button.ID_ENTER;
	public static final int ESCAPE = Button.ID_ESCAPE;
	public static final int LEFT = Button.ID_LEFT;
	public static final int RIGHT = Button.ID_RIGHT;
	public static final int DOLEVA = LEFT;
	public static final int DOPRAVA = RIGHT;

	public static enum Sensor {
		/**
		 * zadny sensor
		 */
		NONE, TOUCH,
		/**
		 * <h1>Priklad pouziti</h1>
		 *
		 * Pristup k prvnimu sensoru pres light1, k dalsim s vyssim cislem (jde o pocet
		 * pouzitych stejnych sensoru, ne o zapojeni do portu). </br>
		 * </br>
		 *
		 * <b>light1.readNormalizedValue()</b> vrati 0 (tma) az 1023 (nejvetsi svetlo)
		 * <br/>
		 * <b>light1.setFloodlight(true)</b> rozsviti cervene svetlo<br/>
		 * <b>light1.setFloodlight(false)</b> zhasne cervene svetlo<br/>
		 */
		LIGHT, SONAR
	}

	public static enum MotorDirection {
		FORWARD, INVERSE
	}

	public static enum MotorInterwork {
		TANDEM, COUNTER
	}

	/* Matematicke funkce a konstanty */

	public static float max(float a, float b) {
		if (a > b)
			return a;
		else
			return b;
	}

	public static int max(int a, int b) {
		if (a > b)
			return a;
		else
			return b;
	}

	public static float min(float a, float b) {
		if (a < b)
			return a;
		else
			return b;
	}

	public static int min(int a, int b) {
		if (a < b)
			return a;
		else
			return b;
	}

	public static float relu(float x) {
		if (x > 0)
			return x;
		else
			return 0;
	}

	public static int relu(int x) {
		if (x > 0)
			return x;
		else
			return 0;
	}

	public static float sgn(float x) {
		if (x > 0)
			return 1;
		else
			return -1;
	}

	public static int sgn(int x) {
		if (x > 0)
			return 1;
		else
			return -1;
	}

	public static float abs(float x) {
		return Math.abs(x);
	}

	public static int abs(int x) {
		return Math.abs(x);
	}

	public static float sqr(float x) {
		return x * x;
	}

	public static int sqr(int x) {
		return x * x;
	}

	public static float sqrt(float x) {
		return (float) Math.sqrt(x);
	}

	public static float sqrt(double x) {
		return (float) Math.sqrt(x);
	}

	public static float exp(float x) {
		return (float) Math.exp(x);
	}

	public static float sin(float x) {
		return (float) Math.sin(x);
	}

	public static float cos(float x) {
		return (float) Math.cos(x);
	}

	public static float tan(float x) {
		return (float) Math.tan(x);
	}

	public static float log(float x) {
		return (float) Math.log(x);
	}

	public static float asin(float x) {
		return (float) Math.asin(x);
	}

	public static float acos(float x) {
		return (float) Math.acos(x);
	}

	public static float atan(float x) {
		return (float) Math.atan(x);
	}

	public static float round(float x) {
		return Math.round(x);
	}

	public static int iround(float x) {
		return (int) Math.round(x);
	}

	public static float exp(double x) {
		return (float) Math.exp(x);
	}

	public static float sin(double x) {
		return (float) Math.sin(x);
	}

	public static float cos(double x) {
		return (float) Math.cos(x);
	}

	public static float tan(double x) {
		return (float) Math.tan(x);
	}

	public static float log(double x) {
		return (float) Math.log(x);
	}

	public static float asin(double x) {
		return (float) Math.asin(x);
	}

	public static float acos(double x) {
		return (float) Math.acos(x);
	}

	public static float atan(double x) {
		return (float) Math.atan(x);
	}

	public static final float PI = 3.141592653589f;
	public static final float RAD2DEG = 180f / PI;
	public static final float DEG2RAD = PI / 180f;
	public static final float MAX_SPEED_DPS = 2850; // maximalni rychlost [deg/s] pro go, turn a sledovani cary

	/******************* Ovladani motoru */

	public static class NXTRegMotor extends NXTRegulatedMotor {
		private static enum MotorState {
			STOP, BWD, FWD
		}

		private MotorState lastState;

		/**
		 * Vytvori instanci motoru
		 *
		 * @param b port ke kteremu je pripojeny
		 */
		public NXTRegMotor(MotorPort b) {
			super(b);
			lastState = MotorState.STOP;
		}

		/**
		 * Nastav pozadovanou rychlost toceni motoru. 0=stop, >0=vpred, <0=vzad. Motor
		 * se podle zadaneho cisla rozjeden nebo zastavi. Pohyb musi byt ukoncen pomoci
		 * motX.stop() nebo pomoci dalsiho volani motX.setSpeed(). Pokud se misto toho
		 * driv zavola rotateTo() nebo rotate() (dokud se motor jeste toci nasledkem
		 * setSpeed()) nemusi dalsi setSpeed() fungovat podle ocekavani!!!
		 *
		 * @param degPerSecond rychlost v stupnich za sekundu, muze byt zaporna nebo
		 *                     nulova.
		 */
		public void setSpeed(int degPerSecond) {
			if (degPerSecond == 0) {
				if (lastState != MotorState.STOP) {
					lastState = MotorState.STOP;
					super.stop(true);
				}
			} else if (degPerSecond > 0) {
				if (lastState != MotorState.FWD) {
					forward();
				}
				super.setSpeed(degPerSecond);
				lastState = MotorState.FWD;
			} else {
				if (lastState != MotorState.BWD) {
					backward();
				}
				super.setSpeed(-degPerSecond);
				lastState = MotorState.BWD;
			}
		}

		/**
		 * Nastav pozadovanou rychlost toceni motoru bez toho aby motor zapomel, kde ma
		 * zastavit, pokud se v okamziku volani funkce uz toci. Pokud se vsak motor
		 * netoci, nebude spusten. Bere pouze kladna cisla (pri zadani zaporneho
		 * ignoruje znamenko).
		 *
		 * POZOR: Pokud se nastavi rychlost prilis nizko, dojde k "zamrznuti" motoru!!!!
		 *
		 * @param degPerSecond pozadovana rychlost ve stupnich za sekundu - pouzije se
		 *                     absolutni hodnota ze zadaneho cisla a motor se toci porad
		 *                     ve stejnem smeru.
		 */
		public void origsetSpeed(float degPerSecond) {
			super.setSpeed(degPerSecond);
		}

		/**
		 * Zastav motor brzdenim
		 *
		 * @param immediate true: hned vyskocit z funkce, false: blokovat dokud nedojde
		 *                  k zastaveni
		 */
		public void brake(boolean immediate) {
			lastState = MotorState.STOP;
			super.stop(immediate);
		}

		public void brake() {
			brake(false);
		}

		/**
		 * Zastav motor, nech prirozene dojet
		 */
		public void neutral(boolean immediate) {
			lastState = MotorState.STOP;
			super.flt(immediate);
		}

		public void neutral() {
			neutral(false);
		}
	}

	private static class CustomLCDOutputStream extends OutputStream {

		private int col = 0;
		private int line = 0;

		@Override
		public void write(int c) {
			char x = (char) (c & 0xFF);
			switch (x) {
			case '\t':
				col = col + 8 - col % 8;
				break;
			case '\n':
				incLine();
			case '\r':
				col = 0;
				break;
			case '\f':
				LCD.clear();
				line = 0;
				col = 0;
				break;
			default:
				if (col >= LCD.DISPLAY_CHAR_WIDTH) {
					col = 0;
					incLine();
				}
				LCD.drawChar(x, col++, line);
			}
		}

		private void incLine() {
			if (line < LCD.DISPLAY_CHAR_DEPTH - 1)
				line++;
			else
				LCD.scroll();
		}
	}

	static {
		PrintStream lcdOut = new PrintStream(new CustomLCDOutputStream());
		System.setOut(lcdOut);
		System.setErr(lcdOut);
	}

	/* Vstup/vystup (klavesnice, obrazovka) */

	/*
	 * poznamka k print/println: z hlediska vykonu neni optimalni brat jako parametr
	 * Object, protoze se musi provest autoboxing. vysledky jsou nicmene stejne a
	 * mene funkci bude delat mensi neporadek v code completion v IDE, co deti
	 * pouzivaji.
	 */

	/**
	 * Vytiskni hodnotu
	 *
	 * @param o co chci vytisknout
	 */
	public static void print(Object o) {
		System.out.print(o);
	}

	/**
	 * Vytiskni hodnotu a ukonci radek
	 *
	 * @param o co chci vytisknout
	 */
	public static void println(Object o) {
		System.out.println(o);
	}

	public static void flush() {
		System.out.flush();
	}

	/******************* Mereni casu, cekani, mutitasking */

	private static Stopwatch _TT;

	/**
	 * Cekej nejakou dobu
	 *
	 * @param miliseconds pocet milisekund, kolik cekat
	 * @return 0, kdyz to dopadlo dobre, -1 pri chybe
	 */
	public static int sleepMilliseconds(int miliseconds) {
		try {
			Thread.sleep(miliseconds);
		} catch (InterruptedException e) {
			return -1;
		}
		return 0;
	}

	public static int msSleep(int miliseconds) {
		return sleepMilliseconds(miliseconds);
	}

	/**
	 * dej procesoru sanci, aby se venoval taky necemu dalsimu
	 */
	public static void yield() {
		Thread.yield();
	}

	/**
	 * Zjisti, kolik ubehlo casu od zacatku behu
	 *
	 * @return pocet milisekund od posledniho zavolani init()
	 */
	public static int elapsedMilliseconds() {
		return _TT.elapsed();
	}

	public static int msTime() {
		return elapsedMilliseconds();
	}

	/**
	 * Pockej na zmacknuti tlacitka a vrat, co bylo zmacknuto
	 *
	 * @return soucet toho, co bylo zmacknuto... 0=nic, 1=enter, 2=left, 4=right,
	 *         8=escape
	 */
	public static int getButton() {
		Button.waitForAnyPress();
		return readButton();
	}

	/**
	 * Vrat, co se aktualne macka
	 *
	 * @return soucet toho, co bylo zmacknuto. Cila stejna jako pro getButton(). Lze
	 *         tez pouzit konstanty ENTER, ESCAPE, LEFT, RIGHT.
	 */
	public static int readButton() {
		return Button.readButtons();
	}

	/**
	 * Nasledujici 2 funkce zkontroluji, jestli tlacitkovy kod getb_code vraceny
	 * funkci readButton() nebo getButton() obsahuje vsechna tlacitka v kombinaci
	 * (funkce allButtons()) nebo nektera z nich (funkce anyButton()). Kombinace se
	 * zadava jako soucet tlacitek. Napr.
	 *
	 * if(allButtons(getButton(), LEFT+RIGHT)){ print("L+R"); }else{ print("neco
	 * jineho"); }
	 *
	 * ceka na stisk tlacitek a pokud bylo stisknuto LEFT+RIGHT, vypise "L+R", jinak
	 * vypise text "neco jineho". Cekani na konkretni kombinaci klaves
	 * LEFT+ESC+RIGHT se dela takto:
	 *
	 * while(!allButtons(getButton(), LEFT+ESC+RIGHT)) yield();
	 *
	 * Cekani na libovolnou klavesu z ESC, ENTER, RIGHT, takto:
	 *
	 * while(!anyButton(getButton(), ESC+ENTER+RIGHT) yield();
	 *
	 * Kdyz chceme uprostred programu, ktery neco pocita jen otestovat jestli
	 * nahodou neni stlacena kombinace ESC+ENTER, pouzijeme funkci readButton(),
	 * ktera nezpusobi cekani kdyz neni stisknute zadne tlacitko. Napr se to hodi
	 * pro ukonceni cyklu zasahem uzivatele:
	 *
	 * whiel(!allButtons(readButton(), ESC+ENTER)){ // zde je vypocet }
	 *
	 * POZOR: hardware robota dokaze rozpoznat jen kombinace neco+ENTER a navic
	 * ESC+ENTER provadi jeho reset. Takze funguje jen LEFT+ENTER a RIGHT+ENTER!
	 */
	public static boolean allButtons(int getb_code, int b_combination) {
		return (getb_code & b_combination) == b_combination;
	}

	public static boolean anyButton(int getb_code, int b_combination) {
		return (getb_code & b_combination) != 0;
	}

	public static void cls() {
		System.out.print('\f');
	}

	public static int xres() {
		return 100;
	}

	public static int yres() {
		return 64;
	}

	public static void pixel(int color, int x, int y) {
		if (color == 2) {
			color = 1 ^ LCD.getPixel(x, y);
		}
		LCD.setPixel(x, y, color);
	}

	public static void gflush() {
		LCD.refresh();
	}

	public static void autogflush(boolean On) {
		LCD.setAutoRefresh(On);
	}

	/* zvuky */

	/**
	 * pipni
	 */
	public static void beep() {
		Sound.beep();
	}

	/**
	 * zacni prehravat ton
	 * 
	 * @param freq                 frekvence v Hz
	 * @param durationMilliseconds delka v ms
	 * @param volume               hlasitost 0 az 100 (povoli max 70 - ochrana pred
	 *                             utrzenim reproduktoru!)
	 */
	public static void playTone(int freq, int durationMilliseconds, int volume) {
		Sound.playTone(freq, durationMilliseconds, max(70, volume));
	}

	/**
	 * zacni prehravat notu, kde 100 odpovida komornimu A, cili 440 Hz.
	 *
	 * frekvence je 440*(2^(1/12))^(note-100)
	 */
	public static void playNote(float note, int durationMilliseconds, int volume) {
		playTone(iround(440f * exp(0.0577622650466622f * (note - 100))), durationMilliseconds, max(70, volume));
	}

	/**
	 * motory pripojene k jednotlivym portum
	 */
	public static NXTRegMotor motA = new NXTRegMotor(MotorPort.A);
	public static NXTRegMotor motB = new NXTRegMotor(MotorPort.B);
	public static NXTRegMotor motC = new NXTRegMotor(MotorPort.C);

	/* Inicializace zakladnich funkci (sensory a prime ovladani motoru) */

	/*
	 * Priklad pouziti sensoru: TOUCH: touch1.isPressed() vrati true kdyz je
	 * tlacitko stlaceno, jinak false SONAR: sonar.getDistance() vrati 0 az 255 (255
	 * znamena prilis velka vzdalenost nebo chyba) LIGHT:
	 * light1.readNormalizedValue() vrati 0 (tma) az 1023 (nejvetsi svetlo)
	 * light1.setFloodlight(true) rozsviti cervene svetlo
	 * light1.setFloodlight(false) zhasne cervene svetlo offtrack(light1)
	 *
	 * Je mozne mit az 4 tlacika touch1 az touch4, az 2 svetelne sensory light1 a
	 * light2 a jeden sonar. Jejich prirazeni portum se urcuje ve funkci init, napr.
	 *
	 * init(Sensor.LIGHT,Sensor.TOUCH,Sensor.LIGHT,Sensor.SONAR);
	 *
	 * priradi svetlo na port 1 (light1) a 3 (light2), hmat na port 2 (touch1) a
	 * sonar na port 4 (sonar). V zavorce je vzdy napsana cast pred teckou pri
	 * pouzivani.
	 *
	 * Ovladani motoru:
	 *
	 * Motory se oznacuji motA, motB, motC jdou ovladat jednak primo kdy rikam o
	 * kolik se maji otocit anebo je mozne 2 z nich vybrat a priradit je podvozku
	 * robota. Pak je mozne primo rikat kam ma robot jet nebo zatocit.
	 *
	 * Prvni primy zpusob: Jsou pristupne pres motA, motB a motC. Rizeni ukazu na
	 * motA: motA.rotate(x,false) otoci motor o x stupnu (muze byt zaporne i vetsi
	 * nez 360) motA.rotate(x,true) totez ale neceka na dokonceni - program bezi dal
	 * motA.rotateTo(x,false) natoci motor na polohu x stupnu od zacatku (muze byt
	 * zaporne) motA.rotateTo(x,true) totez, ale neceka na dokonceni motA.isMoving()
	 * vrati true kdyz se motor pusteny pomoci rotate(x,true) porad jeste snazi
	 * hybat, protoze jeste nedosahl ciloveho uhlu otoceni. motA.setSpeed(v) nastavi
	 * rychlost otaceni na v stupnu za sekundu motA.stop() okamzite zastavi motor a
	 * drzi ho silou motA.flt() zastavi motor a necha ho dotocit - motor zustane
	 * volne motA.getTachoCount() vrati aktualni polohu motoru ve stupnich. Ve
	 * spojeni s rotateTo(x,true) jde pouzit ke zmereni zateze motoru podle
	 * rychlosti zaberu
	 *
	 * Druhy neprimy zpusob: Nejdriv musim definovat podvozek pomoci
	 *
	 * initBuggy(prumer_kola_v_mm, rozchod_kol_v_mm, levy_motor, pravy_motor)
	 *
	 * takze napr. initBuggy(30,130,motC,motB).
	 *
	 * Pak lze pouzivat go(vzdalenost_v_mm) turn(otoveni_vlevo_ve_stupnich)
	 * speed(rychlost_v_mm_za_sekundu) a acceleration(zrychleni_v_mm_za_s_za_s) pro
	 * hladky rozjezd a brzdeni.
	 * 
	 * Funkce speed() vrati aktualni rychlost v mm/s acceleration() vrati aktualni
	 * zrychleni v (mm/s)/s
	 * 
	 */

	private static int _light_ct, _touch_ct, _sonar_ct;
	public static RobotaborLightSensor light1, light2;
	public static UltrasonicSensor sonar, sonar1, sonar2;
	public static TouchSensor touch1, touch2, touch3, touch4;

	/**
	 * inicializuj knihovnu bez pripojenych sensoru
	 */
	public static void init() {
		init(Sensor.NONE, Sensor.NONE, Sensor.NONE, Sensor.NONE);
	}

	/**
	 * Inicializace knihovny s pripojenim sensoru.
	 *
	 *
	 * @param p1 sensor pripojeny k portu 1
	 * @param p2 sensor pripojeny k portu 2
	 * @param p3 sensor pripojeny k portu 3
	 * @param p4 sensor pripojeny k portu 4
	 */
	public static void init(Sensor p1, Sensor p2, Sensor p3, Sensor p4) {
		print("EasyRobotLibrary v 2026.3\n");
		_TT = new Stopwatch();
		_TT.reset();
		motA.neutral();
		motB.neutral();
		motC.neutral();
		_light_ct = 1;
		_touch_ct = 1;
		_sonar_ct = 1;
		attachSensor(p1, SensorPort.S1);
		attachSensor(p2, SensorPort.S2);
		attachSensor(p3, SensorPort.S3);
		attachSensor(p4, SensorPort.S4);
	}

	private static void attachSensor(Sensor in, SensorPort sp) {
		if (in != Sensor.NONE) {
			if (Sensor.TOUCH == in) {
				TouchSensor t = new TouchSensor(sp);
				switch (_touch_ct) {
				case 1:
					touch1 = t;
					break;
				case 2:
					touch2 = t;
					break;
				case 3:
					touch3 = t;
					break;
				default:
					touch4 = t;
				}
				_touch_ct++;
			} else if (Sensor.LIGHT == in) {
				RobotaborLightSensor l = new RobotaborLightSensor(sp);
				switch (_light_ct) {
				case 1:
					light1 = l;
					break;
				default:
					light2 = l;
				}
				_light_ct++;
			} else {
				UltrasonicSensor s = new UltrasonicSensor(sp);
				switch (_sonar_ct) {
				case 1:
					sonar = s;
					sonar1 = s;
					break;
				default:
					sonar2 = s;
				}
				_sonar_ct++;
			}
		}
	}
	
	public static class RobotaborLightSensor {

		private final LightSensor impl;
		
		public RobotaborLightSensor(ADSensorPort port) {
			this.impl = new LightSensor(port);
		}
		
		public int getLight() {
			return impl.getNormalizedLightValue();
		}
		
		public void setFloodlight(boolean floodlight) {
			impl.setFloodlight(floodlight);
		}
		
		public boolean isFloodlightOn() {
			return impl.isFloodlightOn();
		}
	}

	/******************* Buggy (podvozek robota) */

	private static NXTRegMotor motL, motR;
	private static float _L, _R;
	private static float RAD2DEG_invR, RAD2DEG_inv_absR, half_DEG2RAD_R;
	private static float _L_over_R;
	private static MotorDirection dir;
	private static MotorInterwork sync;

	/**
	 * Inicializuj podvozek robota, predpoklada se ze C je levy motor, B je pravy
	 * motor
	 */
	public static void initBuggy(float prumer_kola, float rozchod_kol) {
		initBuggy(prumer_kola, rozchod_kol, motC, motB);
	}

	public static void initBuggy(float prumer_kola, float rozchod_kol, NXTRegMotor levy_motor,
			NXTRegMotor pravy_motor) {
		MotorDirection dir = prumer_kola >= 0.0f ? MotorDirection.FORWARD : MotorDirection.INVERSE;
		initBuggy(prumer_kola, rozchod_kol, levy_motor, pravy_motor, dir, MotorInterwork.TANDEM);
	}

	/**
	 * Inicializuj podvozek robota.
	 * 
	 * @param prumer  kol v mm. Musi byt nenulovy. Zaporna hodnota znamena ze robot
	 *                ma predek na opacne strane nez je obvykle (abychom nemuseli
	 *                zadavat go(-100) kdyz chceme jet o 100 mm dopredu, ackoliv to
	 *                funguje a sledovani cary pozpatku tez (pokud je sensor na
	 *                zadku robota)).
	 * @param rozchod kol v mm. Musi byt kladny.
	 * @param lmot    Staticka instance leveho motoru.
	 * @param rmot    Staticka instance praveho motoru.
	 * @param dir     Pomer mezi smerem motoru <> jizdy robota.
	 * @param sync    Vztah mezi smerem leveho a praveho motoru.
	 */
	public static void initBuggy(float prumer, float rozchod, NXTRegMotor levy_motor, NXTRegMotor pravy_motor,
			MotorDirection smer_robota, MotorInterwork vztah_motoru) {
		motL = levy_motor;
		motR = pravy_motor;
		// kompatibilita: pokud se zada kladny rozchod, nastavime
		// pri INVERSE na zaporny, aby fungovaly vypocty
		if (smer_robota == MotorDirection.INVERSE) {
			if (prumer > 0.0f) {
				prumer = -prumer;
			}
		}
		dir = smer_robota;
		sync = vztah_motoru;
		_R = prumer * 0.5f;
		_L = rozchod * 0.5f;
		_L_over_R = _L / _R;
		float DEG2RAD_R = _R * DEG2RAD;
		RAD2DEG_invR = 1 / DEG2RAD_R;
		half_DEG2RAD_R = 0.5f * DEG2RAD_R;
		RAD2DEG_inv_absR = abs(RAD2DEG_invR);
		resetCurrentDistance();
		speed(1000);
	}

	/**
	 * Vynuluje mereni ujete vzdalenosti
	 */
	public static void resetCurrentDistance() {
		motR.resetTachoCount();
		motL.resetTachoCount();
	}

	static float target_dist;
	static float way; /* 1 vpred (mm>0), -1 vzad (mm<0) */

	/**
	 * jede vpred mm milimietru, pokud je non_blocking == true, neceka na dokonceni
	 * pohybu
	 */
	public static void go(float mm, boolean non_blocking) {
		target_dist = abs(mm);
		way = (int) sgn(mm);
		float alfa = RAD2DEG_invR * mm;
		int ialfa = iround(alfa); /* zaokrouhleni k nejblizsimu celemu cislu */
		rotateWrtSync(ialfa, ialfa, non_blocking);
	}

	private static float resolveRotationL(float val) {
		return val;
	}

	private static float resolveRotationR(float val) {
		return (sync == MotorInterwork.COUNTER) ? -val : val;
	}

	/**
	 * Rotace s ohledem na nastavenou synchronizaci motoru, tzn. pravy se otoci
	 * opacne, pokud se pouziva COUNTER. Na smer rotace (dir) se nebere ohled, ten
	 * by mel byt zahrnut ve vypoctech parametru predanych do teto funkce,
	 * pravdepodobne skrze signum v polomeru kol.
	 */
	private static void rotateWrtSync(float rotationLDeg, float rotationRDeg, boolean immediateReturn) {
		motL.rotate(Math.round(resolveRotationL(rotationLDeg)), true);
		motR.rotate(Math.round(resolveRotationR(rotationRDeg)), immediateReturn);
	}

	/**
	 * vraci true kdyz probiha pohyb podvozku (napr. zapocaty pomoci go s
	 * non_blocking==true)
	 */
	public static boolean isGoing() {
		return motL.isMoving() || motR.isMoving();
	}

	/**
	 * jede vpred mm milimetru a ceka na dokonceni pohybu nez zacne provadet dalsi
	 * prikaz
	 */
	public static void go(float mm) {
		go(mm, false);
	}

	private static void startLeftMotor(int way) {
		if ((way < 0) ^ (dir == MotorDirection.INVERSE)) {
			motL.backward();
		} else {
			motL.forward();
		}
	}

	private static void startRightMotor(int way) {
		if ((way < 0) ^ (dir == MotorDirection.INVERSE ^ sync == MotorInterwork.COUNTER)) {
			motR.backward();
		} else {
			motR.forward();
		}
	}

	/**
	 * Vyrazi vpred a hned se pokracuje ve vypoctu vaseho programu.
	 */
	public static void forward() {
		startLeftMotor(1);
		startRightMotor(1);
		way = 1;
	}

	/**
	 * Vyrazi vzad
	 */
	public static void backward() {
		startLeftMotor(-1);
		startRightMotor(-1);
		way = -1;
	}

	public static void brake() {
		brake(false);
	}

	/**
	 * Okamzite zastavi a necha zabrzdeny podvozek (brzdi se zadanym zrychlenim).
	 */
	public static void brake(boolean immediateReturn) {
		motL.stop(true);
		motR.stop(true);
		// v2025.7 se nenastavuje way na 0
		// racionale: pouziva se jen v line followeru kde chceme znat
		// posledni smer jizdy, takze po zastaveni chceme nechat hodnotu z minula
		// way = 0f;
	}

	public static void neutral() {
		neutral(false);
	}

	/**
	 * Okamzite odpoji energii od motoru a necha volne dojet.
	 */
	public static void neutral(boolean immediateReturn) {
		motL.flt(true);
		motR.flt(immediateReturn);
		// way = 0f;
	}

	/**
	 * Vrati prumer ujete drahy obou motoru v mm od posledniho zavolani init_buggy()
	 * nebo reset_current_distance(). Kdyz se couvalo napr. pomoci go(-100), vrati
	 * zaporne cislo. Do drahy se pocita i otaceni kole na neutral().
	 */
	public static float getCurrentDistance() {
		int double_angle = motL.getTachoCount() + motR.getTachoCount();
		return half_DEG2RAD_R * double_angle * sgn(_R);
	}

	/**
	 * otoci se vlevo o deg stupnu (zaporny deg toci pochopitelne vpravo)
	 * non_blocking==true neceka na dokonceni pohybu, false ceka.
	 */
	public static void turn(float deg, boolean non_blocking) {
		/*
		 * float mm = (3.141592653589f*deg/180)*_L; float
		 * alfa=(180/3.141592653589f)*mm/_R;
		 */
		float alfa = deg * _L_over_R; /* to je totez, ale da to min prace spocitat */
		int ialfa = iround(alfa);
		rotateWrtSync(-ialfa, ialfa, non_blocking);
	}

	/**
	 * Verze turn() ktera ceka na dokonceni pohybu.
	 */
	public static void turn(float deg) {
		turn(deg, false);
	}

	/**
	 * Zacne se otacet vpravo.
	 */
	public static void turnRight() {
		startLeftMotor(1);
		startRightMotor(-1);
	}

	/**
	 * Zacne se otacet vlevo.
	 */
	public static void turnLeft() {
		startLeftMotor(-1);
		startRightMotor(1);
	}

	static float _1_360 = 1f / 360f;

	private static void arc(float left_mm, float right_mm, float dist, boolean nonBlocking) {
		float speed = motL.getSpeed();
		way = (int) Math.signum(right_mm);
		resetCurrentDistance();
		float time = dist / speed;
		motL.origsetSpeed(left_mm / time);
		motR.origsetSpeed(right_mm / time);
		rotateWrtSync(left_mm * RAD2DEG_invR, right_mm * RAD2DEG_invR, nonBlocking);
		motL.origsetSpeed(speed);
		motR.origsetSpeed(speed);
	}

	/**
	 * Otoci se doleva v oblouku.
	 * 
	 * @param radius       Polomer oblouku = vzdalenost pomyslneho stredu rotacni
	 *                     kruznice po "levici" robota
	 * @param angle        Pocet stupnu ktere ma robot po kruznici ujet. Zaporne
	 *                     jede dozadu.
	 * @param non_blocking true pokud se nema cekat na skonceni
	 */
	public static void arcLeft(float radius, float angle, boolean non_blocking) {
		arc(arcCircumference(radius - _L, angle), arcCircumference(radius + _L, angle), arcCircumference(radius, angle),
				non_blocking);
	}

	/**
	 * Otoci se doprava v oblouku.
	 * 
	 * @param radius       Polomer oblouku = vzdalenost pomyslneho stredu rotacni
	 *                     kruznice po "pravici" robota
	 * @param angle        Pocet stupnu ktere ma robot po kruznici ujet. Zaporne
	 *                     jede dozadu.
	 * @param non_blocking true pokud se nema cekat na skonceni
	 */
	public static void arcRight(float radius, float angle, boolean non_blocking) {
		arc(arcCircumference(radius + _L, angle), arcCircumference(radius - _L, angle), arcCircumference(radius, angle),
				non_blocking);
	}

	public static void arcLeft(float radius, float angle) {
		arcLeft(radius, angle, false);
	}

	public static void arcRight(float radius, float angle) {
		arcRight(radius, angle, false);
	}

	private static float arcCircumference(float r, float a) {
		return (float) (2 * Math.PI * Math.abs(r) * (a * _1_360));
	}

	/**
	 * nastavi rychlost v mm/s (mmps musi byt cislo vetsi nez 0) pro nasledujici
	 * jizdu. Pripadne zmeni rychlost prave probihajici jizdy. Pokud robot stal,
	 * samotne nastaveni rychlosti ho nerozjede (k tomu je potreba go(), forward()
	 * nebo backward()).
	 *
	 * POZOR: Behem sledovani cary se rychlost nesmi menit, protoze samotne
	 * sledovani cary funguje na principu zvysovani a snizovani rychlosti motoru a
	 * popletlo by ho to.
	 */
	public static void speed(float mmps) {
		float stupnu_za_sekundu = RAD2DEG_inv_absR * mmps;
		if (stupnu_za_sekundu > MAX_SPEED_DPS)
			stupnu_za_sekundu = MAX_SPEED_DPS;
		if (stupnu_za_sekundu > 0) {
			motR.origsetSpeed(stupnu_za_sekundu);
			motL.origsetSpeed(stupnu_za_sekundu);
		}
	}

	/*
	 * Vrati prumernou rychlost obou motoru v mm/s
	 */
	public static float speed() {
		int double_speed = motR.getSpeed() + motL.getSpeed();
		return double_speed * abs(half_DEG2RAD_R);
	}

	/*
	 * nastavi rychlost v mm/s zvlast pro leve a prave kolo, oboji musi byt vetsi
	 * nez 0
	 */
	public static void speed(float left_mmps, float right_mmps) {
		float stupnu_za_sekundu = RAD2DEG_inv_absR * left_mmps;
		if (stupnu_za_sekundu > MAX_SPEED_DPS)
			stupnu_za_sekundu = MAX_SPEED_DPS;
		if (stupnu_za_sekundu > 0)
			motL.origsetSpeed(stupnu_za_sekundu);
		stupnu_za_sekundu = RAD2DEG_inv_absR * right_mmps;
		if (stupnu_za_sekundu > MAX_SPEED_DPS)
			stupnu_za_sekundu = MAX_SPEED_DPS;
		if (stupnu_za_sekundu > 0)
			motR.origsetSpeed(stupnu_za_sekundu);
	}

	/**
	 * nastavi se maximalni zrychleni robota
	 *
	 * @param mmpss zrychleni, ktere se muze pouzit (v mm/s/s)
	 */
	public static void acceleration(float mmpss) {
		int a = iround(RAD2DEG_inv_absR * mmpss);
		motR.setAcceleration(a);
		motL.setAcceleration(a);
	}

	/**
	 * Vrati zrychleni v mm/s/s
	 */
	public static float acceleration() {
		return (motL.getAcceleration() + motR.getAcceleration()) * abs(half_DEG2RAD_R);
	}

	/******************* Kalibrace svetelneho sensoru */

	static RobotaborLightSensor follow_ls;
	static int black_level, white_level;

	/* vrati -1 (jsme kompletne na cerne) az 1 (jsme kompletne na bile) */
	public static float offtrack(RobotaborLightSensor ls) {
		int c = ls.getLight();
		return (2 * (float) (c - black_level)) / (white_level - black_level) - 1;
	}

	/*
	 * Polozit robota cidlem na bilou, vpravo od cerne (cidlo je na predku robota,
	 * jinak obracne) tak aby cervene svetlo promitane na zem bylo zcela na bile a
	 * svym okrajem bylo blizko cerne. Kalibrace zacne otacet robotem vlevo o zadany
	 * uhel, cimz prejede od bile do cerne a zmeri si minimum a maximum, ktere pak
	 * bude pouzivat. Je vhodne pro tuto operaci nastavit mensi rychlost a zrychleni
	 * pomoci speed() a reduce_acceleration() a pak je zase treba zvysi pro jizdu.
	 */
	public static void calibrateBuggy(RobotaborLightSensor ls, int uhel) {
		turn(uhel, true);
		int pokracovat = 2;
		follow_ls = ls;
		black_level = 65536;
		white_level = 0;
		while (pokracovat > 0) {
			int jas = ls.getLight();
			if (jas < black_level)
				black_level = jas;
			if (jas > white_level)
				white_level = jas;
			if (!isGoing()) {
				if (pokracovat > 1) {
					pokracovat = 1;
					turn(-uhel, true);
				} else {
					pokracovat = 0;
				}
			}
		}
		if (white_level <= black_level) {
			beep();
			msSleep(120);
			print("selhani kalibrace\n");
			beep();
			msSleep(20);
			beep();
			getButton();
			getButton();
		}
		print("B=");
		print(black_level);
		print(" W=");
		print(white_level);
		print("\n");
	}

	/*
	 * Pokud jsme si jisti ze stojime na bile barve, je mozne to vyuzit ke zpresneni
	 * kalibrace svetelneho sensoru tak aby sledovala menici-se svetelne podminky,
	 * tim ze zavolame tuto funkci.
	 */
	public static void recalibrateOnWhite() {
		int posunuti = follow_ls.getLight() - white_level;
		white_level = white_level + posunuti;
		black_level = black_level + posunuti;
	}

	/******************* Sledovani cary */

	static float follow_p, follow_i, follow_d, follow_blacksat, follow_whitesat, slowdown_decay;
	static float forward_speed_degps;
	static int last_time, last_print_time, isat;
	static float last_e, acc_e, slowdown, lpf_slowdown, lpf_diff, prelpf_diff;

	enum Fstate {
		OK, SLOWING, SLOW
	};

	static Fstate fstate;
	static int state_change_time;
	static boolean debug_follower = false;

	public static void startFollowing(float p, float i, float d, float bsat, float wsat, float sdecay) {
		follow_p = p;
		follow_i = i;
		follow_d = d;
		follow_blacksat = bsat;
		follow_whitesat = wsat;
		slowdown_decay = sdecay;
		slowdown = 0;
		lpf_slowdown = 0;
		lpf_diff = 0;
		prelpf_diff = 0;
		forward_speed_degps = sgn(way * _R) * motL.getSpeed();
		float abs_rel_speed = abs(forward_speed_degps) * 0.003f;
		follow_i *= abs_rel_speed;
		follow_d /= abs_rel_speed;
		print("Fspeed=");
		print(iround(forward_speed_degps / RAD2DEG_inv_absR));
		print(" mm/s\n");
		last_time = msTime();
		last_print_time = last_time - 1000;
		last_e = offtrack(follow_ls);
		acc_e = 0;
		isat = 0;
		fstate = Fstate.OK;
		state_change_time = last_time;
	}

	/*
	 * Dat robota napravo od cary mirit mirne sikmo na caru. Zavolat go(x,true) a
	 * zavolat find_track(). Funkce se vrati jakmile najede na cernou. Pak je treba
	 * zavolat start_following a volat dokola follow(), kdyz se blizime ke konci tak
	 * zavolat end_following a pak brake()
	 */
	public static void findTrack(float searchSpeed) {
		speed(searchSpeed);
		forward();
		while (offtrack(follow_ls) > -.3) {
			yield();
		}
	}

	/* jede po cerne care, na jejim pravem okraji */
	public static void follow() {
		int now = msTime();
		if (now - last_time < 8) {
			msSleep(10 - (now - last_time));
			now = msTime();
		}
		float dt = 0.001f * (now - last_time);
		float e = offtrack(follow_ls); /* 1=bila -1=cerna, cil je 0 */
		if (dt > 0.1f) {
			beep(); // chyba -- musite to volat aspon 10* za sekundu
		} else {
			float diff = follow_d * (e - last_e) / dt;
			prelpf_diff = diff * 0.2f + 0.8f * prelpf_diff;
			lpf_diff = prelpf_diff * 0.1f + 0.9f * lpf_diff;
			acc_e = acc_e + follow_i * e * dt;
			if (e > 1.1f)
				e = 1.1f;
			else if (e < -1.1f)
				e = -1.1f;
			if (acc_e > follow_whitesat)
				isat = 1;
			else if (acc_e < follow_blacksat)
				isat = -1;
			if (acc_e > 1)
				acc_e = 1;
			else if (acc_e < -1)
				acc_e = -1;
			if (isat > 0 && e < 0.12f) { /* navrat z bile saturace pri prejeti cerne cary */
				isat = 0;
				acc_e = 0;
			} else if (isat < 0 && e > -0.12f) { /* navrat z cerne saturace pri dosazeni bile */
				isat = 0;
				acc_e = 0;
			}
			float a = e * follow_p + acc_e + lpf_diff; /* a je akce, cili relativni rychlost zataceni vlevo */
			float e2 = relu(e * e - 0.1f) * (1 / 0.9f);
			lpf_slowdown = e2 * e2 * slowdown_decay + lpf_slowdown * (1f - slowdown_decay);
			slowdown = slowdown * slowdown_decay + lpf_slowdown * (1f - slowdown_decay);
			if (slowdown > 1)
				slowdown = 1;
			float common_speed = RAD2DEG_inv_absR * forward_speed_degps;
			if (fstate == Fstate.OK) {
				if (slowdown > 0.6f) {
					state_change_time = now;
					fstate = Fstate.SLOWING;
				}
			} else if (fstate == Fstate.SLOWING) {
				if (now - state_change_time > 600)
					fstate = Fstate.SLOW;
			} else { // fstate==Fstate.SLOW
				if (slowdown < 0.4f)
					fstate = Fstate.OK;
				common_speed = 240; // deg/s
			}
			float ls = common_speed * (1 - a * way - slowdown);
			float rs = common_speed * (1 + a * way - slowdown);
			motR.setSpeed(iround(rs));
			motL.setSpeed(iround(ls));
			if (now - last_print_time > 250 && debug_follower) {
				if (e > 0)
					print("e");
				print(iround(e * 100));
				if (e > 0)
					print(" a");
				print(iround(a * 100));
				if (e > 0)
					print(" i");
				print(iround(acc_e * 100));
				print(" \r");
				last_print_time = now;
			}
		}
		last_time = now;
		last_e = e;
		yield();
	}

	static void stopFollowing() {
		motR.setSpeed(iround(forward_speed_degps));
		motL.setSpeed(iround(forward_speed_degps));
	}
}
