package iconloop.client;

import iconloop.client.menu.Menu;
import iconloop.client.util.Clue;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@Command(name = "client", mixinStandardHelpOptions = true, version = "1.0",
        description = "Print the segmented information.")
class SampleClient implements Callable<Integer>{
    public static void main(String[] args) {
        int result = new CommandLine(new SampleClient()).execute(args);
        System.exit(result);
    }
    @Option(names = {"-m", "--mode"}, description = "Cli Test Mode. 0: client test mode, 1: command line clue mode, default: 1")
    private int mode = 1;

    @Option(names = {"-p", "--print"}, description = "Print Result, 0: no Show, 1: Show, default: 0")
    private int print_mode = 0;

    @Option(names = {"-t", "--text"}, description = "Text Information")
    private String plain_text = "";

    @Option(names = {"-f", "--file"}, description = "file information")
    private String file_path = "";

    @Option(names = {"-th", "--threshHolder"}, description = "Minimum Holder number")
    private int thresh_holder = 2;

    @Option(names = {"-n", "--number"}, description = "Divided Number")
    private int number = 3;

    @Override
    public Integer call() throws Exception {
        if (mode == 0) {
            Menu menu = new Menu();
            menu.selectedMenu();
            return 1;
        } else if (mode == 1) {
            File result = new File("./clues.txt");

            try (BufferedWriter out = new BufferedWriter(new FileWriter(result))) {
                String err_msg = "";
                if (number < 2 || number < thresh_holder) {
                    err_msg = "Storage Number or Number is wrong";
                    out.write(err_msg);
                    throw new Exception(err_msg);
                }

                if ("".equals(plain_text) && "".equals(file_path)) {
                    err_msg = "Need text information";
                    out.write(err_msg);
                    throw new Exception(err_msg);
                }

                if (!"".equals(file_path)) {
                    File include_file = new File(file_path);
                    if (!include_file.exists()) {
                        err_msg = "File Not exists";
                        out.write(err_msg);
                        throw new Exception(err_msg);
                    }

                    BufferedReader in = new BufferedReader((new FileReader(include_file)));
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        plain_text = String.format("%s\n%s", plain_text, line);
                    }
                }

                Clue clue = new Clue();
                String[] clues = clue.makeClue(number, thresh_holder, plain_text.getBytes(StandardCharsets.UTF_8));
                if (print_mode == 1) {
                    System.out.println(String.join("\n", clues));

                    // 복호화 테스트 정보.
                    byte[] reconstructed = clue.reconstruct(number, thresh_holder, clues);
                    String reconstructedStr = new String(reconstructed, StandardCharsets.UTF_8);
                    System.out.println(reconstructedStr);
                }

                out.write(String.join("\n", clues));
                return 1;
            }
        }

        return 1;
    }
}
