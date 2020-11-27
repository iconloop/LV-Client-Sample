package iconloop.client.menu;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Scanner;
import iconloop.client.communication.CommunicateManager;
import iconloop.client.communication.CommunicateStorage;
import iconloop.client.util.Clue;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    List<String> saveClues = new ArrayList<>();
    String inputSharedString;

    public void selectedMenu() {
        List<String> menuList = new ArrayList<>();
        menuList.add("1. Manager Server");
        menuList.add("2. Storage Server");
        boolean isLoop = true;
        while(isLoop) {
            switch (showMenu(menuList)) {
                case 2:
                    storageMenu();
                    break;
                case 1:
                    managerMenu();
                    break;
                case 0:
                    isLoop = false;
                    break;
            }
        }
    }

    public void managerMenu() {
        List<String> menuList = new ArrayList<>();
        menuList.add("1. Request Auth");

        CommunicateManager cmManager = new CommunicateManager();
        boolean isLoop = true;
        while(isLoop) {
            switch (showMenu(menuList)) {
                case 1:
                    cmManager.requestIssueVC();
                    break;
                case 0:
                    isLoop = false;
                    break;
            }
        }
    }

    public void storageMenu() {
        List<String> menuList = new ArrayList<>();
        menuList.add("1. Request Token");
        menuList.add("2. Request Store Clue");
        menuList.add("3. Request Clue Info");

        CommunicateStorage cmStorage = new CommunicateStorage();
        boolean isLoop = true;
        while(isLoop) {
            switch (showMenu(menuList)) {
                case 3:
                    reconstruct();
                    break;
                case 2:
                    dataSharing();
                    break;
                case 1:
                    cmStorage.requestToken();
                    break;
                case 0:
                    isLoop = false;
                    break;
            }
        }

    }

    public void dataSharing() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Input Sharing Data >> ");
        String sharingInfo = sc.next();
        Clue clue = new Clue();
        CommunicateStorage cmStorage = new CommunicateStorage();

        String[] clues = clue.makeClue(3, 2, sharingInfo.getBytes(Charset.forName("UTF-8")));

        for(String clueString : clues) {
            System.out.println("Clue : " + clueString);
            saveClues.add(clueString);
            cmStorage.requestStoreClue(clueString);
        }
    }

    public void reconstruct() {
        Clue clue = new Clue();
        CommunicateStorage cmStorage = new CommunicateStorage();

        // TODO : Using Storage Information.
        // Fixme : Sample Code
        int storage_num = 3;

        String[] clues = new String[storage_num];
        int idx = 0;
        for(String clueString : saveClues) {
            // TODO : Using really response clue data.
            cmStorage.requestToken();
            clues[idx++] = clueString;
        }

        byte[] reconstructed = clue.reconstruct(3, 2, clues);
        String reconstructedStr = new String(reconstructed, Charset.forName("UTF-8"));
        System.out.println(reconstructedStr);
    }

    public int showMenu(List<String> menuList) {
        Scanner sc = new Scanner(System.in);

        int selected_number = -1;

        while(0 > selected_number || selected_number > menuList.size()) {
            System.out.println("==========================================");
            for(String menu : menuList) {
                System.out.println(menu);
            }

            System.out.println("0. Quit");
            System.out.println("==========================================");
            System.out.print("Input Number >> ");
            selected_number = sc.nextInt();
        }

        return selected_number;
    }
}
