package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigInteger;
import java.net.URL;
import java.util.*;

/**
 * Author: Jan-Willem van Bremen 500779265
 */

public class Controller implements Initializable {

    static Scanner sc = new Scanner(System.in);
    static List<Character> map = new ArrayList<>();
    // The first prime number
    public static final BigInteger INIT_NUMBER = new BigInteger("2");

    // FXML Fields
    @FXML
    public Button btn_step1_e;
    @FXML
    public Button btn_step2_e;
    @FXML
    public Button btn_step3_e;
    @FXML
    public TextField tv_n_e;
    @FXML
    public TextField tv_m_e;
    @FXML
    public TextField tv_p_e;
    @FXML
    public TextField tv_q_e;
    @FXML
    public TextField tv_n_d;
    @FXML
    public TextField tv_c_d;
    @FXML
    public TextField tv_e_d;
    @FXML
    public Button btn_step1_d;
    @FXML
    public Button btn_step2_d;
    @FXML
    public TextArea ta_console;

    // Variables
    BigInteger n, p, q, n2, e, d = new BigInteger("0");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initList();
        addTextFieldListeners();

        //Encryption steps
        btn_step1_e.setOnAction(event -> step1_e());
        btn_step2_e.setOnAction(event -> step2_e());
        btn_step3_e.setOnAction(event -> step3_e());

        //Decryption steps
        btn_step1_d.setOnAction(event -> step1_d());
        btn_step2_d.setOnAction(event -> step2_d());
    }

    //Step 1 of Encryption: Generate p and q from given n
    private void step1_e() {
        //Check if value of n has been entered
        if (tv_n_e.getText().isEmpty()) {
            log("Enter value of n.");
            return;
        }

        this.n = new BigInteger(tv_n_e.getText());

        long startTime = System.nanoTime();
        log("step 1: ");
        log("Calculating p & q...");

        BigInteger p = INIT_NUMBER;

        //For each prime p
        while(p.compareTo(n.divide(INIT_NUMBER)) <= 0){
            //If we find p
            if(n.mod(p).equals(BigInteger.ZERO)){
                //Calculate q
                BigInteger q = n.divide(p);
                //The end of the algorithm, assign variables and log results
                this.p = p;
                this.q = q;
                this.tv_p_e.setText(this.p.toString());
                this.tv_q_e.setText(this.q.toString());
                log("p: " + p);
                log("q: " + q);
                long endTime = System.nanoTime();
                log("Amount of time busy finding p and q: " + (endTime - startTime) / 1000000 + "ms");
                this.btn_step2_e.setDisable(false);
                return;
            }
            //p = the next prime number
            p = p.nextProbablePrime();
        }
        log("No solution exists!");
    }

    //Step 2 of Encryption: Generate e
    private void step2_e() {
        log("Step 2: ");
        this.n2 = this.p.subtract(BigInteger.ONE).multiply(this.q.subtract(BigInteger.ONE));

        int y, intGCD;

        Random x = new Random();
        BigInteger e;

        do {
            y = x.nextInt(this.n2.intValue() - 1);
            String z = Integer.toString(y);
            e = new BigInteger(z);
            BigInteger gcd = this.n2.gcd(e);
            intGCD = gcd.intValue();
        } while (y <= 2 || intGCD != 1);

        this.e = e;
        this.tv_e_d.setText(e.toString());
        log("e: " + this.e);
        this.btn_step3_e.setDisable(false);
    }

    //Step 3 of Encryption: Encrypt the message
    private void step3_e() {
        String message = tv_m_e.getText();

        if (message.isEmpty()) {
            log("Fill in message.");
            return;
        }

        char[] characterArray = message.toCharArray();

        List<Integer> encryptedMessage = new ArrayList<>();

        for (char character : characterArray) {

            Character unencryptedChar = getCharacterByChar(character);

            //Encryption
            int encryptedIndex = unencryptedChar.index.pow(this.e.intValue()).mod(this.n).intValue();
            encryptedMessage.add(encryptedIndex);
        }

        tv_c_d.setText(encryptedMessage.toString());
        log("Message after encryption: " + encryptedMessage.toString());
    }

    //Step 1 of Decryption: Generate d from e and n2
    private void step1_d() {
        if (!this.tv_n_e.getText().isEmpty() || !this.tv_e_d.getText().isEmpty()) {
            this.e = new BigInteger(tv_e_d.getText());
            this.d = this.e.modInverse(this.n2); // Multiplicative inverse
            log("d: " + this.d);
            this.btn_step2_d.setDisable(false);
        } else {
            log("Missing n or e.");
        }
    }

    //Step 2 of Decryption: Decrypt the message c
    private void step2_d() {
        String c = tv_c_d.getText();

        if (c.isEmpty()) {
            log("Fill in message.");
            return;
        }

        String[] EncryptedMessage = c
                .replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll(" ", "")
                .split(",");

        List<Integer> decryptedMessage = new ArrayList<>();
        List<java.lang.Character> decryptedText = new ArrayList<>();

        for (String encryptedIndex : EncryptedMessage) {
            //Decryption
            int decryptedIndex =
                    BigInteger.valueOf(Integer.parseInt(encryptedIndex)).pow(this.d.intValue()).mod(this.n).intValue();
            decryptedMessage.add(decryptedIndex);

            //Lookup indexes in characters
            if (!Objects.isNull(getCharacterByIndex(decryptedIndex))) {
                decryptedText.add(getCharacterByIndex(decryptedIndex).character);
            } else {
                log("Error decrypting message.");
                return;
            }
        }
        log("Message after decryption is: " + decryptedText.toString());
    }

    private Character getCharacterByChar(char Character) {
        for (Character character : map) {
            if (character.character == Character) {
                return character;
            }
        }
        return null;
    }

    private Character getCharacterByIndex(int index) {
        for (Character character : map) {
            if (character.index.intValue() == index) {
                return character;
            }
        }
        return null;
    }

    private void addTextFieldListeners() {
        tv_n_e.textProperty().addListener((observable, oldValue, newValue) -> {
            tv_n_e.setText(numericOnlyFilter(newValue));
            tv_n_d.setText(numericOnlyFilter(newValue));
        });
        tv_m_e.textProperty().addListener((observable, oldValue, newValue) -> {
            tv_m_e.setText(lettersOnlyFilter(newValue).toUpperCase());
        });
        tv_p_e.textProperty().addListener(((observable, oldValue, newValue) -> {
            tv_p_e.setText(numericOnlyFilter(newValue));
            if (!tv_p_e.getText().isEmpty()) { this.p = new BigInteger(tv_p_e.getText()); }
        }));
        tv_q_e.textProperty().addListener(((observable, oldValue, newValue) -> {
            tv_q_e.setText(numericOnlyFilter(newValue));
            if (!tv_q_e.getText().isEmpty()) { this.q = new BigInteger(tv_q_e.getText()); }
        }));
        tv_n_d.textProperty().addListener((observable, oldValue, newValue) -> {
            tv_n_d.setText(numericOnlyFilter(newValue));
            tv_n_e.setText(numericOnlyFilter(newValue));
        });
        tv_e_d.textProperty().addListener((observable, oldValue, newValue) -> {
            tv_e_d.setText(numericOnlyFilter(newValue));
            if (!tv_e_d.getText().isEmpty()) { this.e = new BigInteger(tv_e_d.getText()); }
        });
    }

    private String numericOnlyFilter(String newValue) {
        if (!newValue.matches("\\d*")) {
            return newValue.replaceAll("[^\\d]", "");
        } else {
            return newValue;
        }
    }

    private String lettersOnlyFilter(String newValue) {
        if (!newValue.matches("\\sa-zA-Z*")) {
            return newValue.replaceAll("[^\\sa-zA-Z]", "");
        } else {
            return newValue;
        }
    }

    private void log(String text) {
        if (!ta_console.getText().isEmpty()) {
            ta_console.appendText("\n" + text);
        } else {
            ta_console.setText(text);
        }
        ta_console.setScrollTop(Double.MAX_VALUE);
    }

    private void initList() {
        map.add(new Character('A', BigInteger.valueOf(1)));
        map.add(new Character('B', BigInteger.valueOf(2)));
        map.add(new Character('C', BigInteger.valueOf(3)));
        map.add(new Character('D', BigInteger.valueOf(4)));
        map.add(new Character('E', BigInteger.valueOf(5)));
        map.add(new Character('F', BigInteger.valueOf(6)));
        map.add(new Character('G', BigInteger.valueOf(7)));
        map.add(new Character('H', BigInteger.valueOf(8)));
        map.add(new Character('I', BigInteger.valueOf(9)));
        map.add(new Character('J', BigInteger.valueOf(10)));
        map.add(new Character('K', BigInteger.valueOf(11)));
        map.add(new Character('L', BigInteger.valueOf(12)));
        map.add(new Character('M', BigInteger.valueOf(13)));
        map.add(new Character('N', BigInteger.valueOf(14)));
        map.add(new Character('O', BigInteger.valueOf(15)));
        map.add(new Character('P', BigInteger.valueOf(16)));
        map.add(new Character('Q', BigInteger.valueOf(17)));
        map.add(new Character('R', BigInteger.valueOf(18)));
        map.add(new Character('S', BigInteger.valueOf(19)));
        map.add(new Character('T', BigInteger.valueOf(20)));
        map.add(new Character('U', BigInteger.valueOf(21)));
        map.add(new Character('V', BigInteger.valueOf(22)));
        map.add(new Character('W', BigInteger.valueOf(23)));
        map.add(new Character('X', BigInteger.valueOf(24)));
        map.add(new Character('Y', BigInteger.valueOf(25)));
        map.add(new Character('Z', BigInteger.valueOf(26)));
    }
}
