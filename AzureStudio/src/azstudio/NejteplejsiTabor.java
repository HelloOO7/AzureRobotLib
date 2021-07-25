package azstudio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class NejteplejsiTabor {
	public static void main(String[] args){
		try {
			List<Guy> guys = new ArrayList<>();
			Scanner s = new Scanner(new File("tab.csv"));
			while (s.hasNextLine()){
				String guy = s.nextLine();
				String[] stuff = guy.split(";");
				Guy g = new Guy();
				if (stuff[0].length() == 0 || stuff.length < 2){
					continue;
				}
				g.temperature = Float.parseFloat(stuff[0].replace(",", "."));
				int ss = 1;
				if (stuff[1].length() == 1){
					ss = 0;
				}
				switch (stuff[1].substring(0, 1)){
					case "H":
						g.tabor = Tabor.Hudebni;
						break;
					case "V":
						switch (stuff[1]){
							case "VV":
								g.isVedouci = true;
							case "V":
								g.tabor = Tabor.Vytvarny;
								break;
							case "VE":
								g.tabor = Tabor.Org;
								break;
						}
						break;
					case "R":
						g.tabor = Tabor.Robotak;
						break;
					case "M":
						g.tabor = Tabor.Modelky;
						break;
					case "F":
						g.tabor = Tabor.Fotaci;
						break;
					case "A":
						g.tabor = Tabor.Adrenalin;
						break;
				}
				if (stuff[1].length() == 2){
					g.isVedouci = stuff[1].charAt(1) == 'V';
				}
				guys.add(g);
			}
			s.close();

			Map<Tabor, Integer> guyCount = new HashMap<>();
			Map<Tabor, Float> guyTemps = new HashMap<>();

			for (Tabor t : Tabor.values()){
				guyCount.put(t, 0);
				guyTemps.put(t, 0f);
			}

			for (Guy guy : guys){
				if (guy.tabor == null){
					continue;
				}
				guyCount.put(guy.tabor, guyCount.get(guy.tabor) + 1);
				guyTemps.put(guy.tabor, guyTemps.get(guy.tabor) + guy.temperature);
			}

			float highest = 0f;
			Tabor highestTabor = null;
			for (Guy guy : guys){
				System.out.println(guy.temperature);
				if (guy.isVedouci){
					if (guy.temperature > highest){
						highestTabor = guy.tabor;
						highest = guy.temperature;
					}
				}
			}
			System.out.println("Nejteplejsi vedouci ma " + highest + " stupnu a je z " + highestTabor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class Guy{
		public float temperature;
		public Tabor tabor;
		public boolean isVedouci;
	}

	private static enum Tabor{
		Hudebni,
		Robotak,
		Modelky,
		Adrenalin,
		Vytvarny,
		Fotaci,
		Org
	}
}
