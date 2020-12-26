import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Apriori {
	public static void main(String[] args) throws IOException {
		ArrayList<String> itemsNames = new ArrayList<String>();// this array will contain the products names
		ArrayList<String> itemSets = new ArrayList<String>();// this array will contain each transaction item set
																// separately

		// reading data from file
		BufferedReader reader = new BufferedReader(new FileReader("CoffeeShopTransactions.txt"));
		String line = new String();
		while ((line = reader.readLine()) != null) {
			// here i used % as delimiter because there are some products name contain
			// spaces
			String[] temp = line.split("%");
			for (String item : temp) {
				if (!itemsNames.contains(item))
					itemsNames.add(item);
			}
			// adding the whole item set to the item sets array
			itemSets.add(temp[0] + " " + temp[1] + " " + temp[2]);
		}
		reader.close();

		// taking minimum support and confidence from user
		Scanner input = new Scanner(System.in);
		System.out.print("Enter the minimum Support and minimum Confidence\n support: ");
		double mSup = input.nextDouble();
		System.out.print(" confidence: ");
		double mConf = input.nextDouble();
		System.out.println("where do you want to print the result 1-files 0-console : ");
		boolean files = false;
		if (input.next().equals("1"))
			files = true;

		// Generating 1st table
		if (files)
			System.setOut(new PrintStream("FirstTable.txt"));
		System.out.println("First table:\n" + String.format("%-16s", "Item") + "Support count");
		for (int i = 0; i < itemsNames.size(); i++) {
			int count = calcOneItemSupport(itemSets, itemsNames.get(i));
			System.out.print(String.format("%-16s", itemsNames.get(i)) + count);
			System.out.print("\t-->\t" + String.format("%.4f", (double) count / itemSets.size()));
			if ((double) count / itemSets.size() < mSup) {
				System.out.println("\texcluded");
				itemsNames.remove(i);
				i--;
				// i need to decrease the iterator by one step because removing
				// element will shift the array and change array size
			} else
				System.out.println();
		}

		// Generating 2nd table
		if (files)
			System.setOut(new PrintStream("SecondTable.txt"));
		ArrayList<String> freqTwoItemSets = new ArrayList<String>();
		if (itemsNames.size() >= 2) {
			System.out.println("\nSecond table:\n" + String.format("%-32s", "Itemset") + "Support count");
			for (int i = 0; i < itemsNames.size() - 1; i++) {
				for (int j = i + 1; j < itemsNames.size(); j++) {
					int count = calcTwoItemsSupport(itemSets, itemsNames.get(i), itemsNames.get(j));
					System.out.print(String.format("%-32s", itemsNames.get(i) + " , " + itemsNames.get(j)) + count);
					System.out.print("\t-->\t" + String.format("%.4f", (double) count / itemSets.size()));
					if ((double) count / itemSets.size() < mSup)
						System.out.println("\texcluded");
					else {
						System.out.println();
						freqTwoItemSets.add(itemsNames.get(i) + " , " + itemsNames.get(j));
					}
				}
			}
		} else
			System.out.println("\nwe need at least 2 items satisfy the min support to genrate the second table");

		// Generating 3rd table
		if (files)
			System.setOut(new PrintStream("ThirdTable.txt"));
		ArrayList<String> freqThreeItemSets = new ArrayList<String>();
		if (itemsNames.size() >= 3 && freqTwoItemSets.size() >= 2) {
			System.out.println("\nThird table:\n" + String.format("%-48s", "Itemset") + "Support count");
			for (int i = 0; i < itemsNames.size() - 2; i++) {
				for (int j = i + 1; j < itemsNames.size() - 1; j++) {
					for (int k = j + 1; k < itemsNames.size(); k++) {
						// making sure that every subset of this triplet is a frequent Two-Item set
						if (freqTwoItemSets.contains(itemsNames.get(i) + " , " + itemsNames.get(j))
								&& freqTwoItemSets.contains(itemsNames.get(i) + " , " + itemsNames.get(k))
								&& freqTwoItemSets.contains(itemsNames.get(j) + " , " + itemsNames.get(k))) {
							int count = calcThreeItemsSupport(itemSets, itemsNames.get(i), itemsNames.get(j),
									itemsNames.get(k));
							System.out.print(String.format("%-48s",
									itemsNames.get(i) + " , " + itemsNames.get(j) + " , " + itemsNames.get(k)) + count);
							System.out.print("\t-->\t" + String.format("%.4f", (double) count / itemSets.size()));
							if ((double) count / itemSets.size() < mSup)
								System.out.println("\texcluded");
							else {
								System.out.println();
								freqThreeItemSets
										.add(itemsNames.get(i) + " , " + itemsNames.get(j) + " , " + itemsNames.get(k));
							}
						}
					}
				}
			}
		} else
			System.out.println("\nwe need at least 2 frequent Two-item set to genrate triplets");

		// generating rules
		if (files)
			System.setOut(new PrintStream("FrequentSets&AssociationRules.txt"));
		if (itemsNames.size() < 2)// in case all items excluded in the first table or except one item
			System.out.println(
					"\nwe need at least 2 frequent items to generate association rules and calculate thier confidence");
		else {
			if (freqTwoItemSets.size() > 0 || freqThreeItemSets.size() > 0) {
				System.out.println("\nAll the frequent item sets:\n");
				for (int i = 0; i < freqTwoItemSets.size(); i++)
					System.out.println(freqTwoItemSets.get(i));
				for (int i = 0; i < freqThreeItemSets.size(); i++)
					System.out.println(freqThreeItemSets.get(i));
				System.out.println("\nAll the association rules with thier confidence:\n");
			} else
				System.out.println("\nno association rules (no item sets or all of them were excluded)");
			if (freqTwoItemSets.size() >= 1) {// extracting rules for two-item sets
				for (int i = 0; i < freqTwoItemSets.size(); i++) {
					// splitting items
					String[] items = freqTwoItemSets.get(i).split(" , ");
					String first = items[0];
					String second = items[1];
					// calculating whole support
					int sup = calcTwoItemsSupport(itemSets, first, second);

					// each two-item set generates 2 rules
					System.out.println(String.format("%-33s", first + " -> " + second) + "\tconfidence = " + sup + "/"
							+ calcOneItemSupport(itemSets, first) + " = "
							+ String.format("%.4f", (double) sup / calcOneItemSupport(itemSets, first))
							+ checkConf(mConf, (double) sup / calcOneItemSupport(itemSets, first)));

					System.out.println(String.format("%-33s", second + " -> " + first) + "\tconfidence = " + sup + "/"
							+ calcOneItemSupport(itemSets, second) + " = "
							+ String.format("%.4f", (double) sup / calcOneItemSupport(itemSets, second))
							+ checkConf(mConf, (double) sup / calcOneItemSupport(itemSets, second)));
				}
			}
			if (freqThreeItemSets.size() >= 1) {// extracting rules for triplets
				for (int i = 0; i < freqThreeItemSets.size(); i++) {
					// splitting items
					String[] items = freqThreeItemSets.get(i).split(" , ");
					String first = items[0];
					String second = items[1];
					String third = items[2];
					// calculating whole support
					int sup = calcThreeItemsSupport(itemSets, first, second, third);
					// each triplet generates 6 rules

					// 1 -> 2&3
					System.out.println(String.format("%-49s", first + " -> " + second + " & " + third)
							+ "\tconfidence = " + sup + "/" + calcOneItemSupport(itemSets, first) + " = "
							+ String.format("%.4f", (double) sup / calcOneItemSupport(itemSets, first))
							+ checkConf(mConf, (double) sup / calcOneItemSupport(itemSets, first)));

					// 2 -> 1&3
					System.out.println(String.format("%-49s", second + " -> " + first + " & " + third)
							+ "\tconfidence = " + sup + "/" + calcOneItemSupport(itemSets, second) + " = "
							+ String.format("%.4f", (double) sup / calcOneItemSupport(itemSets, second))
							+ checkConf(mConf, (double) sup / calcOneItemSupport(itemSets, second)));

					// 3 -> 1&2
					System.out.println(String.format("%-49s", third + " -> " + first + " & " + second)
							+ "\tconfidence = " + sup + "/" + calcOneItemSupport(itemSets, third) + " = "
							+ String.format("%.4f", (double) sup / calcOneItemSupport(itemSets, third))
							+ checkConf(mConf, (double) sup / calcOneItemSupport(itemSets, third)));

					// 1&2 -> 3
					System.out.println(String.format("%-49s", first + " & " + second + " -> " + third)
							+ "\tconfidence = " + sup + "/" + calcTwoItemsSupport(itemSets, first, second) + " = "
							+ String.format("%.4f", (double) sup / calcTwoItemsSupport(itemSets, first, second))
							+ checkConf(mConf, (double) sup / calcTwoItemsSupport(itemSets, first, second)));

					// 1&3 -> 2
					System.out.println(String.format("%-49s", first + " & " + third + " -> " + second)
							+ "\tconfidence = " + sup + "/" + calcTwoItemsSupport(itemSets, first, third) + " = "
							+ String.format("%.4f", (double) sup / calcTwoItemsSupport(itemSets, first, third))
							+ checkConf(mConf, (double) sup / calcTwoItemsSupport(itemSets, first, third)));

					// 2&3 -> 1
					System.out.println(String.format("%-49s", second + " & " + third + " -> " + first)
							+ "\tconfidence = " + sup + "/" + calcTwoItemsSupport(itemSets, second, third) + " = "
							+ String.format("%.4f", (double) sup / calcTwoItemsSupport(itemSets, second, third))
							+ checkConf(mConf, (double) sup / calcTwoItemsSupport(itemSets, second, third)));
				}
			}
		}
	}

	private static int calcOneItemSupport(ArrayList<String> itemSets, String item1) {
		int count = 0;
		for (int i = 0; i < itemSets.size(); i++) {
			if (itemSets.get(i).contains(item1))
				count++;
		}
		return count;
	}

	private static int calcTwoItemsSupport(ArrayList<String> itemSets, String item1, String item2) {
		int count = 0;
		for (int i = 0; i < itemSets.size(); i++) {
			if (itemSets.get(i).contains(item1) && itemSets.get(i).contains(item2))
				count++;
		}
		return count;
	}

	private static int calcThreeItemsSupport(ArrayList<String> itemSets, String item1, String item2, String item3) {
		int count = 0;
		for (int i = 0; i < itemSets.size(); i++) {
			if (itemSets.get(i).contains(item1) && itemSets.get(i).contains(item2) && itemSets.get(i).contains(item3))
				count++;
		}
		return count;
	}

	private static String checkConf(double conf, double _conf) {
		if (_conf >= conf)
			return "\tStrong";
		return "\tWeak";
	}
}
