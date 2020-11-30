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
    private final String[] storageList = {
            "http://127.0.0.1:8100/vault",
            "http://127.0.0.1:8200/vault",
            "http://127.0.0.1:8300/vault"
    };

    private final int storageNumber = storageList.length;
    private String vID = "";

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
        menuList.add("1. requestIssueVID");
        menuList.add("2. Request Issue VC");

        CommunicateManager cmManager = new CommunicateManager();
        boolean isLoop = true;

        while(isLoop) {
            switch (showMenu(menuList)) {
                case 2:
                    cmManager.requestIssueVC(vID);
                    break;
                case 1:
                    vID = cmManager.requestIssueVID();
                    vID = vID.substring(1, vID.length()-1);
                    System.out.println(vID);
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
                    requestToken();
                    break;
                case 0:
                    isLoop = false;
                    break;
            }
        }

    }

    public void requestToken() {
        System.out.println("\nRequest.....");
        for(int i=0; i<storageNumber; ++i) {
            System.out.println(">>> " + storageList[i]);
            CommunicateStorage cmStorage = new CommunicateStorage(storageList[i]);
            cmStorage.requestToken();
            System.out.println("\n\n");
        }
    }

    public void dataSharing() {
        if( "".equals(vID) ) {
            System.out.println("It must be need vID. It process to get vID.");
            return ;
        }

        Scanner sc = new Scanner(System.in);

        System.out.print("\n\nInput Sharing Data >> ");
        String sharingInfo = sc.next();
        Clue clue = new Clue();
        String[] clues = clue.makeClue(storageNumber, 2, sharingInfo.getBytes(Charset.forName("UTF-8")));

        System.out.println("\nRequest.....");
        for(int i=0; i<storageNumber; ++i) {
            System.out.println(">>> " + storageList[i]);
            CommunicateStorage cmStorage = new CommunicateStorage(storageList[i]);
            cmStorage.requestStoreClue(clues[i], vID);
            System.out.println("\n\n");
        }
    }

    public void reconstruct() {
        if( "".equals(vID) ) {
            System.out.println("It must be need vID. It process to get vID.");
            return ;
        }

        Clue clue = new Clue();

        String[] clues = new String[storageNumber];
        System.out.println("\nRequest.....");
        for(int i=0; i<storageNumber; ++i) {
            System.out.println(">>> " + storageList[i]);
            CommunicateStorage cmStorage = new CommunicateStorage(storageList[i]);
            String resultClue = cmStorage.requestClue(vID);
            clues[i] = resultClue.substring(1, resultClue.length()-1);
            System.out.println("\n\n");
        }

        byte[] reconstructed = clue.reconstruct(storageNumber, 2, clues);
        String reconstructedStr = new String(reconstructed, Charset.forName("UTF-8"));

        System.out.println("Reconstruct Data \n\t- " + reconstructedStr);
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
            try {
                selected_number = sc.nextInt();
            }catch(Exception e){
                System.out.println("Not menu....");
                sc.next();
            }
        }

        return selected_number;
    }
}
