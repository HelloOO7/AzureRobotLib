
import lejos.nxt.*;
import lejos.util.Stopwatch;

public abstract class Robotabor {

	private static final boolean IFDEF_DEBUG = false;

	private static final float PI = 3.141592653589f;
	private static final float RAD_TO_DEG = 180f / PI;
	private static final float DEG_TO_RAD = PI / 180f;

	private static float common_clamp450(float value) {
		return (value > 450f) ? 450f : value;
	}

	private static float common_clamp5_450(float value) {
		if (value > 450f) {
			return 450f;
		} else if (value < 5f) {
			return 5f;
		}
		return value;
	}

	private static void debugPrint(Object str){
		if (IFDEF_DEBUG){
			print(str);
		}
	}
	
	public static class NXTRegMotor extends NXTRegulatedMotor {

		private MotorState last_state = MotorState.STOP; // -1=backward 0=stop 1=forward

		/**
		 * Vytvori instanci motoru
		 *
		 * @param b port ke kteremu je pripojeny
		 */
		public NXTRegMotor(MotorPort b) {
			super(b);
			last_state = MotorState.STOP;
		}

		/**
		 * Nastav pozadovanou rychlost toceni motoru
		 *
		 * @param degPerSecond rychlost v stupnich za sekundu
		 */
		public void setSpeed(int degPerSecond) {
			if (degPerSecond == 0) {
				if (last_state != MotorState.STOP) {
					last_state = MotorState.STOP;
					super.stop(true);
				}
			} else if (degPerSecond > 0) {
				if (last_state != MotorState.FWD) {
					forward();
				}
				super.setSpeed(degPerSecond);
				last_state = MotorState.FWD;
			} else {
				if (last_state != MotorState.BWD) {
					backward();
				}
				super.setSpeed(-degPerSecond);
				last_state = MotorState.BWD;
			}
		}

		/**
		 * Nastav pozadovanou rychlost toceni motoru -- nepouzivat, neupravena puvodni funkce
		 *
		 * @param degPerSecond pozadovana rychlost ve stupnich za sekundu
		 */
		public void origsetSpeed(int degPerSecond) {
			super.setSpeed(degPerSecond);
		}

		/**
		 * Zastav motor brzdenim
		 *
		 * @param immediate true: hned vyskocit z funkce, false: blokovat dokud nedojde k zastaveni
		 */
		public void stop(boolean immediate) {
			last_state = MotorState.STOP;
			super.stop(immediate);
		}

		/**
		 * Zastav motor, nech prirozene dojet
		 */
		public void flt() {
			last_state = MotorState.STOP;
			super.flt();
		}
		
		private static enum MotorState {
			STOP,
			BWD,
			FWD
		}
	}

	private static Stopwatch _TT;

	/**
	 * Vytiskni hodnotu
	 *
	 * @param b co chci vytisknout
	 */
	public static void print(boolean b) {
		System.out.print(b);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param c co chci vytisknout
	 */
	public static void print(char c) {
		System.out.print(c);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param i co chci vytisknout
	 */
	public static void print(int i) {
		System.out.print(i);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param l co chci vytisknout
	 */
	public static void print(long l) {
		System.out.print(l);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param f co chci vytisknout
	 */
	public static void print(float f) {
		System.out.print(f);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param d co chci vytisnkout
	 */
	public static void print(double d) {
		System.out.print(d);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param a co chci vytisknout
	 */
	public static void print(char[] a) {
		System.out.print(a);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param s co chci vytisknout
	 */
	public static void print(String s) {
		System.out.print(s);
	}

	/**
	 * Vytiskni hodnotu
	 *
	 * @param o co chci vytisknout
	 */
	public static void print(Object o) {
		System.out.print(o);
	}

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

	/**
	 * dej procesoru sanci, aby se venoval taky necemu dalsimu
	 */
	public static void yield() {
		Thread.yield();
	}

	/**
	 * Zjisti, kolik ubehlo casu od zacatku behu
	 *
	 * @return pocet milisekund od zacatku
	 */
	public static int elapsedMilliseconds() {
		return _TT.elapsed();
	}

	/* returns current time in milliseconds */
	/**
	 * Pockej na zmacknuti tlacitka a vrat, co bylo zmacknuto
	 *
	 * @return soucet toho, co bylo zmacknuto... 0=nic, 1=enter, 2=left, 4=right, 8=escape
	 */
	public static int getButton() {
		Button.waitForAnyPress();
		return readButton();
	}

	/**
	 * Vrat, co se aktualne macka
	 *
	 * @return soucet toho, co bylo zmacknuto... 0=nic, 1=enter, 2=left, 4=right, 8=escape
	 */
	public static int readButton() {
		return Button.readButtons();
	}

	/* 1=ENTER | 2=LEFT | 4=RIGHT | 8=ESCAPE */
	/**
	 * pipni
	 */
	public static void beep() {
		Sound.beep();
	}

	/**
	 * motory pripojene k jednotlivym portum
	 */
	public static NXTRegMotor motA, motB, motC;

	/*
	 * Priklad pouziti sensoru: 
	 * TOUCH: touch1.isPressed() vrati true kdyz je
	 * tlacitko stlaceno, jinak false SONAR: sonar.getDistance() vrati 0 az 255
	 * (255 znamena prilis velka vzdalenost nebo chyba)
	 *
	 * Je mozne mit az 4 tlacika touch1 az touch4, az 2 svetelne sensory light1
	 * a light2 a jeden sonar. Jejich prirazeni portum se urcuje ve funkci init,
	 * napr. init(SNS.LIGHT,SNS.TOUCH,SNS.LIGHT,SNS.SONAR); priradi svetlo na
	 * port 1 (light1) a 3 (light2), hmat na port 2 (touch1) a sonar na port 4
	 * (sonar). V zavorce je vzdy napsana cast pred teckou pri pouzivani.
	 *
	 * Priklad pouziti motoru: 
	 * Bud se da pouzit init_buggy(polomer_kola, rozchod_kol) pro motory B a C. 
	 * Pak lze pouzivat go(vzdalenost_v_mm), turn(otoveni_vlevo_ve_stupnich), 
	 * speed(rychlost_v_mm_za_sekundu) a reduce_acceleration() pro hladky rozjezd a brzdeni, nebo je mozne ridit
	 * motory primo. 
	 * Jsou pristupne pres motA, motB a motC. Rizeni ukazu na motA: 
	 *		motA.rotate(x,false) otoci motor o x stupnu (muze byt zaporne i vetsi nez 360) 
	 *		motA.rotate(x,true) totez ale neceka na dokonceni - program bezi dal 
	 *      motA.rotateTo(x,false) natoci motor na polohu x stupnu od zacatku (muze byt zaporne)
	 *      motA.rotateTo(x,true) totez, ale neceka na dokonceni
	 *      motA.isMoving() vrati true pokud se motor pusteny pomoci
	 *      rotate(x,true) porad jeste snazi hybat
	 *      motA.setSpeed(v) nastavi rychlost otaceni na v stupnu za sekundu
	 *      motA.stop() okamzite zastavi motor a drzi ho silou
	 *      motA.flt() zastavi motor a necha ho dotocit - motor zustane volne
	 *      motA.getTachoCount() vrati aktualni polohu motoru ve stupnich. Ve
	 *				spojeni s rotateTo(x,true) jde pouzit ke zmereni zateze motoru podle
	 *				rychlosti zaberu
	 */
	public enum SENSOR {
		/**
		 * zadny sensor
		 */
		NONE,
		TOUCH,
		/**
		 * <h1>Priklad pouziti</h1>
		 *
		 * Pristup k prvnimu sensoru pres light1, k dalsim s vyssim cislem (jde o pocet pouzitych stejnych sensoru, ne o zapojeni do portu). </br>
		 * </br>
		 *
		 * <b>light1.readNormalizedValue()</b> vrati 0 (tma) az 1023 (nejvetsi svetlo) <br/>
		 * <b>light1.setFloodlight(true)</b> rozsviti cervene svetlo<br/>
		 * <b>light1.setFloodlight(false)</b> zhasne cervene svetlo<br/>
		 */
		LIGHT,
		SONAR
	};

	private static int _light_ct, _touch_ct, _sonar_ct;
	public static LightSensor light1, light2;
	public static UltrasonicSensor sonar, sonar1, sonar2;
	public static TouchSensor touch1, touch2, touch3, touch4;

	private static void attachSensor(SENSOR in, SensorPort sp) {
		if (in != SENSOR.NONE) {
			if (SENSOR.TOUCH == in) {
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
			} else if (SENSOR.LIGHT == in) {
				LightSensor l = new LightSensor(sp);
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

	/**
	 * inicializuj knihovnu bez pripojenych sensoru
	 */
	public static void init() {
		init(SENSOR.NONE, SENSOR.NONE, SENSOR.NONE, SENSOR.NONE);
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
	public static void init(SENSOR p1, SENSOR p2, SENSOR p3, SENSOR p4) {
		debugPrint("EasyRobotLibrary v 2020.2\n");
		_TT = new Stopwatch();
		_TT.reset();
		motA = new NXTRegMotor(MotorPort.A);
		motA.flt();
		motB = new NXTRegMotor(MotorPort.B);
		motB.flt();
		motC = new NXTRegMotor(MotorPort.C);
		motC.flt();
		_light_ct = 1;
		_touch_ct = 1;
		_sonar_ct = 1;
		attachSensor(p1, SensorPort.S1);
		attachSensor(p2, SensorPort.S2);
		attachSensor(p3, SensorPort.S3);
		attachSensor(p4, SensorPort.S4);
	}

	/**
	 * Inicializuj rizeni motoru pro robota, predpoklada se ze C je levy motor, B je pravy motor
	 *
	 * @param wheelDiameter prumer kola
	 * @param wheelDistance vzdalenost mezi koly
	 */
	public static void initBuggy(float wheelDiameter, float wheelDistance) {
		initBuggy(wheelDiameter, wheelDistance, motC, motB);
	}

	/**
	 * pristup k rizeni celeho robota
	 */
	public static Buggy buggy;

	/**
	 * Inicializuj rizeni motoru pro robota
	 *
	 * @param wheelDiameter prumer kola
	 * @param wheelDistance vzdalenost mezi koly
	 * @param leftMotor ke kteremu portu je pripojen levy motor (napr. motC)
	 * @param rightMotor ke kteremu portu je pripojen pravy motor (napr. motB)
	 */
	public static void initBuggy(float wheelDiameter, float wheelDistance, NXTRegMotor leftMotor,
		NXTRegMotor rightMotor) {
		buggy = new Buggy(wheelDiameter, wheelDistance, leftMotor, rightMotor);
	}

	private static int black_level, white_level;
	private static float follow_p, follow_i, follow_d;
	private static float forward_speed;
	private static int last_time;
	private static LightSensor follow_ls;
	private static float last_e, acc_e;

	/**
	 * Rekni jak moc jsi na cerne nebo na bile
	 *
	 * @param ls ktery light sensor pouzit (napr. light1)
	 * @return vrati -1 (jsme kompletne na cerne) az 1 (jsme kompletne na bile)
	 */
	public static float getOfftrackValue(LightSensor ls) {
		int c = ls.readNormalizedValue();
		return (2 * (float) (c - black_level)) / (white_level - black_level) - 1;
	}

	/**
	 * Kalibruj svetelny sensor robota
	 *
	 * Polozit robota cidlem na bilou, vpravo od cerne (cidlo je na predku robota, jinak obracne) tak,
	 * aby cervene svetlo promitane na zem bylo zcela na bile a svym okrajem bylo blizko cerne. 
	 * Kalibrace zacne otacet robotem vlevo o zadany uhel, cimz prejede od bile do cerne a zmeri si minimum a maximum, ktere pak bude pouzivat. 
	 * Je vhodne pro tuto operaci nastavit mensi rychlost a zrychleni pomoci speed() a reduce_acceleration() a pak je zase treba zvysi pro jizdu.
	 *
	 * @param ls ktery light sensor pouzit (napr. light1)
	 * @param angleStep uhel o ktery se v jednom kroku otoci robot, kdyz hleda cernou a bilou
	 */
	public static void calibrateBuggy(LightSensor ls, int angleStep) {
		buggy.turn(angleStep, true);
		int pokracovat = 2;
		follow_ls = ls;
		black_level = 65536;
		white_level = 0;
		while (pokracovat > 0) {
			int jas = ls.readNormalizedValue();
			if (jas < black_level) {
				black_level = jas;
			}
			if (jas > white_level) {
				white_level = jas;
			}
			if (!buggy.isGoing()) {
				if (pokracovat > 1) {
					pokracovat = 1;
					buggy.turn(-angleStep, true);
				} else {
					pokracovat = 0;
				}
			}
		}
		if (white_level <= black_level) {
			beep();
			sleepMilliseconds(120);
			debugPrint("selhani kalibrace\n");
			beep();
			sleepMilliseconds(20);
			beep();
			getButton();
			getButton();
		}
		debugPrint("B=");
		debugPrint(black_level);
		debugPrint(" W=");
		debugPrint(white_level);
		debugPrint("\n");
	}

	public static void recalibrateOnWhite() {
		int posunuti = follow_ls.readNormalizedValue() - white_level;
		white_level = white_level + posunuti;
		black_level = black_level + posunuti;
	}

	public static void startFollowing(float p, float i, float d) {
		follow_p = p;
		follow_i = i;
		follow_d = d;
		forward_speed = buggy.motL.getSpeed(); // pripadne *sgn(target_distance)
		debugPrint("forward speed=");
		debugPrint(forward_speed);
		debugPrint("\n");
		sleepMilliseconds(250);
		last_time = elapsedMilliseconds();
		last_e = getOfftrackValue(follow_ls);
		acc_e = 0;
	}

	/*
	 * Dat robota napravo od cary mirit mirne sikmo na caru. Zavolat go(x,true)
	 * a zavolat find_track(). Funkce se vrati jakmile najede na cernou. Pak je
	 * treba zavolat start_following a volat dokola follow(), kdyz se blizime ke
	 * konci tak zavolat end_following a pak brake()
	 */
	public static void findTrack(float searchSpeed) {
		buggy.speed(searchSpeed);
		while (getOfftrackValue(follow_ls) > -.3) {
			yield();
		}
	}

	/* jede po cerne care, na jejim pravem okraji */
	static void follow() {
		int now = elapsedMilliseconds();
		if (now - last_time < 10) {
			sleepMilliseconds(12 - (now - last_time));
			now = elapsedMilliseconds();
		}
		float dt = 0.001f * (now - last_time);
		float invDt = 1f / dt;
		float e = getOfftrackValue(follow_ls);
		/* 1=bila -1=cerna, cil je 0 */
		if (dt > 0.1f) {
			beep(); // chyba -- musite to volat aspon 10* za sekundu
		} else {
			acc_e = acc_e + follow_i * e * dt;
			if (acc_e >= 1) {
				acc_e = 1;
			}
			if (acc_e <= -1) {
				acc_e = -1;
			}
			float diff = follow_d * (e - last_e) * invDt;
			float a = e * follow_p + acc_e
				+ diff;
			/* a=relativni sila zataceni vlevo */
			if (a > 1) {
				a = 1;
			}
			if (a < -1) {
				a = -1;
			}
			float ls = RAD_TO_DEG * (forward_speed * (1 - a * buggy.way) * buggy.invR);
			float rs = RAD_TO_DEG * (forward_speed * (1 + a * buggy.way) * buggy.invR);
			ls = common_clamp5_450(ls);
			rs = common_clamp5_450(rs);
			debugPrint("a=" + a + " acce=" + acc_e + " \r");
			buggy.motR.origsetSpeed((int) rs);
			buggy.motL.origsetSpeed((int) ls);
		}
		last_time = now;
		last_e = e;
		yield();
	}

	public static void stopFollowing() {
		buggy.speed(forward_speed);
	}

	public static class Buggy {

		private NXTRegMotor motL, motR;
		private float _R, _L;
		
		private float invR;
		private float RAD_TO_DEG_invR;

		public Buggy(float prumer, float rozchod, NXTRegMotor levyMotor, NXTRegMotor pravyMotor) {
			motL = levyMotor;
			motR = pravyMotor;
			_R = prumer * 0.5f;
			_L = rozchod * 0.5f;
			invR = 1f / _R;
			RAD_TO_DEG_invR = RAD_TO_DEG * invR;
			speed(1000);
		}

		private float way;
		/* 1 vpred (mm>0), -1 vzad (mm<0) */
		private float target_dist;

		/**
		 * jede vpred mm milimietru
		 *
		 * @param millimeters o kolik milimetru mam popojet
		 * @param nonBlocking true: zadej ukol a hned vyzkoc z volani prikazu, false: pockej, dokud se nedojede zadana vzdalenost
		 */
		public void go(float millimeters, boolean nonBlocking) {
			target_dist = Math.abs(millimeters);
			way = (int) Math.signum(millimeters);
			motR.resetTachoCount();
			motL.resetTachoCount();
			float alfa = millimeters * RAD_TO_DEG_invR;
			int ialfa = Math.round(alfa);
			/* zaokrouhleni k nejblizsimu cislu */
			motR.rotate(ialfa, true);
			motL.rotate(ialfa, nonBlocking);
		}

		/**
		 * vrati absolutni hodnotu aktualni ujetou vzdalenost v mm od posledniho zavolani go() nebo turn() Vraci vzdy kladne cislo, i kdyz se couvalo napr. pomoci go(-100);
		 */
		public float getCurrentDistance() {
			int double_angle = motL.getTachoCount() + motR.getTachoCount();
			return Math.abs(DEG_TO_RAD * 0.5f * double_angle * _R);
		}

		/**
		 * Popojede o zadanou vzdalenost
		 *
		 * @param mm o kolik popojet
		 */
		public void go(float mm) {
			go(mm, false);
		}

		/**
		 * Vrati, zda se robot hybe nebo ne
		 *
		 * @return true: pokud se hybe, jinak false
		 */
		public boolean isGoing() {
			return motL.isMoving() || motR.isMoving();
		}

		/**
		 * Zpomali a zastavi na draze.
		 *
		 * Celkovou jizdni drahu je nutne predem nastavit metodou go(float, bool). Brzdeni si ukradne vzdalenost z konce jizdni drahy:
		 *
		 * ZACATEK DRAHY |--jizda--|--brake_dist--|--discard_dist--| KONEC DRAHY
		 *
		 * Vzdalenost discard_dist bude "zahozena", tj. misto aby na ni robot brzdil uz ji ani neujede.
		 *
		 * @param brake_dist Delka brzdne drahy.
		 * @param discard_dist Delka zbytku brzdne drahy, kdy uz jsou motory zastavene.
		 */
		public void brake(float brake_dist, float discard_dist) {
			float brake_start_dist = target_dist - (brake_dist + discard_dist);
			float brake_end_dist = target_dist - discard_dist;
			if (brake_start_dist > 0) {
				while (getCurrentDistance() < brake_start_dist) {
					yield();
				}
			}
			float invBrakeDist = 1f / brake_dist;
			while (true) {
				float where_am_i = getCurrentDistance();
				speed(5 + forward_speed * (brake_end_dist - where_am_i) * invBrakeDist);
				//Linearni interpolace mezi forward_speed a nulou na draze brakeDist
				if (where_am_i >= brake_end_dist) {
					motL.stop();
					motR.stop();
					break;
				}
				sleepMilliseconds(100);
			}
			speed(forward_speed);
		}

		/**
		 * otoci se vlevo o deg stupnu (zaporny deg toci pochopitelne vpravo)
		 *
		 * @param deg uhel ve stupnich, o ktery se otocit
		 * @param non_blocking true: vrat hned jak zadas ukol, false: pockej, nez se dokonci
		 */
		public void turn(float deg, boolean non_blocking) {
			motR.resetTachoCount();
			motL.resetTachoCount();
			/*
			 * float mm = (3.141592653589f*deg/180)*_L; float
			 * alfa=(180/3.141592653589f)*mm/_R;
			 */
			float alfa = deg * _L * invR;
			/* to je totez, ale da to min prace spocitat */
			int ialfa = Math.round(alfa);
			motR.rotate(ialfa, true);
			motL.rotate(-ialfa, non_blocking);
		}

		/**
		 * Otoci se vlevo o deg stupnu, pocka, nez se dootaci
		 *
		 * @param deg stupnu o ktere se otocit
		 */
		public void turn(float deg) {
			turn(deg, false);
		}

		/**
		 * Zacne se otacet vlevo.
		 */
		public void turn() {

			motL.forward();
			motR.backward();
		}

		/**
		 * Rozjede se dopredu
		 */
		public void forward() {
			motL.forward();
			motR.forward();
		}

		/**
		 * Rozjede se dozadu
		 */
		public void backward() {
			motL.backward();
			motR.backward();
		}

		/**
		 * Zastavi robota brzdenim
		 */
		public void stop() {
			motL.stop();
			motR.stop();
		}

		/**
		 * Zastavi robota odpojenim motoru
		 */
		public void flt() {
			motL.flt();
			motR.flt();
		}

		private void setSpeedBothInternal(float degps) {
			motR.setSpeed(degps);
			motL.setSpeed(degps);
		}

		/**
		 * nastavi rychlost v mm/s
		 */
		public void speed(float mmps) {
			float stupnu_za_sekundu = common_clamp450(mmps * RAD_TO_DEG_invR);
			setSpeedBothInternal(stupnu_za_sekundu);
		}

		/**
		 * nastavi rychlost v mm/s zvlast pro leve a prave kolo
		 */
		public void speed(float left_mmps, float right_mmps) {
			motL.setSpeed(common_clamp450(RAD_TO_DEG_invR * left_mmps));
			motR.setSpeed(common_clamp450(RAD_TO_DEG_invR * right_mmps));
		}

		/**
		 * nastavi se maximalni zrychleni robota
		 *
		 * @param mmpss zrychleni, ktere se muze pouzit (v mm/s^2)
		 */
		public void acceleration(float mmpss) // maximalni zrychleni robota v
		// mm/s/s
		{
			int a = Math.round(RAD_TO_DEG_invR * mmpss);
			motR.setAcceleration(a);
			motL.setAcceleration(a);
		}
	}
}
