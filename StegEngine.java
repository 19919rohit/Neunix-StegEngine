import javax.crypto.*;
import javax.crypto.spec.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.*;

/*
 * =====================================================
 *  NEUNIX STEGENGINE v1.0
 *  PNG LSB Steganography + AES-256 Encryption
 * =====================================================
 */

public class StegEngine {

    /* ================= CONFIG ================= */
    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final int PBKDF2_ITER = 65536;
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 16;
    private static final byte[] MAGIC = "NXSTEG".getBytes(); // signature

    /* ================= MAIN ================= */
    public static void main(String[] args) {
        try {
            if (args.length > 0) cli(args);
            else interactive();
        } catch (Exception e) {
            error("Fatal: " + e.getMessage());
        }
    }

    /* ================= CLI MODE ================= */
    private static void cli(String[] a) throws Exception {
        String in = null, out = null, embed = null, pass = null;
        boolean embedMode = false, extractMode = false;

        for (int i = 0; i < a.length; i++) {
            switch (a[i]) {
                case "-i": in = a[++i]; break;
                case "-o": out = a[++i]; break;
                case "-e": embed = a[++i]; embedMode = true; break;
                case "-x": extractMode = true; break;
                case "-p": pass = a[++i]; break;
                default: error("Unknown arg: " + a[i]);
            }
        }

        if (embedMode) {
            require(in, embed, out);
            hidePNG(
                new File(in),
                new File(out),
                read(new File(embed)),
                pass
            );
            success("Embedded successfully → " + out);
        } else if (extractMode) {
            require(in, out);
            write(
                new File(out),
                extractPNG(new File(in), pass)
            );
            success("Extracted successfully → " + out);
        } else {
            error("Use -e or -x");
        }
    }

    /* ================= INTERACTIVE ================= */
    private static void interactive() throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            clear();
            title();
            println("1. Embed data");
            println("2. Extract data");
            println("3. Exit");
            print("Select: ");

            switch (sc.nextLine().trim()) {
                case "1": embedMenu(sc); break;
                case "2": extractMenu(sc); break;
                case "3": return;
                default: pause("Invalid choice");
            }
        }
    }

    private static void embedMenu(Scanner sc) throws Exception {
        clear();
        println("Embed Menu");
        println("Supported: Any Text File / PNG / WAV ");
        print("Carrier PNG path: ");
        File carrier = new File(sc.nextLine().trim());

        print("File to embed: ");
        File payload = new File(sc.nextLine().trim());

        print("Output PNG path: ");
        File out = new File(sc.nextLine().trim());

        print("Password (optional): ");
        String pass = sc.nextLine().trim();
        if (pass.isEmpty()) pass = null;

        hidePNG(carrier, out, read(payload), pass);
        pause("Embedded successfully");
    }

    private static void extractMenu(Scanner sc) throws Exception {
        clear();
        println("Extract Menu");
        print("Input PNG path: ");
        File in = new File(sc.nextLine().trim());

        print("Output file path: ");
        File out = new File(sc.nextLine().trim());

        print("Password (optional): ");
        String pass = sc.nextLine().trim();
        if (pass.isEmpty()) pass = null;

        write(out, extractPNG(in, pass));
        pause("Extracted successfully");
    }

    /* ================= PNG STEGO ================= */
    private static void hidePNG(File in, File out, byte[] payload, String pass) throws Exception {
        BufferedImage img = ImageIO.read(in);
        byte[] imgBytes = imgToBytes(img);

        byte[] encrypted = encrypt(payload, pass);
        byte[] packed = pack(encrypted);

        if (packed.length * 8 > imgBytes.length)
            throw new IllegalArgumentException("Payload too large for this PNG");

        int bi = 0;
        for (byte b : packed)
            for (int i = 7; i >= 0; i--)
                imgBytes[bi] = (byte)((imgBytes[bi++] & 0xFE) | ((b >> i) & 1));

        ImageIO.write(bytesToImg(imgBytes, img), "png", out);
    }

    private static byte[] extractPNG(File in, String pass) throws Exception {
        BufferedImage img = ImageIO.read(in);
        byte[] imgBytes = imgToBytes(img);

        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int cur = 0, bits = 0;

        for (byte b : imgBytes) {
            cur = (cur << 1) | (b & 1);
            if (++bits == 8) {
                raw.write(cur);
                cur = bits = 0;
            }
        }

        byte[] unpacked = unpack(raw.toByteArray());
        return decrypt(unpacked, pass);
    }

    /* ================= PACKING ================= */
    private static byte[] pack(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MAGIC);
        out.write(ByteBuffer.allocate(4).putInt(data.length).array());
        out.write(data);
        return out.toByteArray();
    }

    private static byte[] unpack(byte[] raw) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(raw);
        byte[] magic = in.readNBytes(MAGIC.length);
        if (!Arrays.equals(magic, MAGIC))
            throw new SecurityException("Not a valid stego file");

        int len = ByteBuffer.wrap(in.readNBytes(4)).getInt();
        return in.readNBytes(len);
    }

    /* ================= CRYPTO ================= */
    private static byte[] encrypt(byte[] d, String p) throws Exception {
        if (p == null) return d;
        byte[] salt = rnd(SALT_LEN), iv = rnd(IV_LEN);
        Cipher c = Cipher.getInstance(AES_MODE);
        c.init(Cipher.ENCRYPT_MODE, key(p, salt), new IvParameterSpec(iv));
        byte[] enc = c.doFinal(d);
        return concat(salt, iv, enc);
    }

    private static byte[] decrypt(byte[] d, String p) throws Exception {
        if (p == null) return d;
        byte[] salt = Arrays.copyOfRange(d, 0, SALT_LEN);
        byte[] iv = Arrays.copyOfRange(d, SALT_LEN, SALT_LEN + IV_LEN);
        byte[] enc = Arrays.copyOfRange(d, SALT_LEN + IV_LEN, d.length);
        Cipher c = Cipher.getInstance(AES_MODE);
        c.init(Cipher.DECRYPT_MODE, key(p, salt), new IvParameterSpec(iv));
        return c.doFinal(enc);
    }

    private static SecretKey key(String p, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(p.toCharArray(), salt, PBKDF2_ITER, AES_KEY_SIZE);
        return new SecretKeySpec(
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded(),
            "AES"
        );
    }

    /* ================= UTIL ================= */
    private static byte[] imgToBytes(BufferedImage i) {
        byte[] d = new byte[i.getWidth() * i.getHeight() * 3];
        int p = 0;
        for (int y = 0; y < i.getHeight(); y++)
            for (int x = 0; x < i.getWidth(); x++) {
                int r = i.getRGB(x, y);
                d[p++] = (byte)(r >> 16);
                d[p++] = (byte)(r >> 8);
                d[p++] = (byte) r;
            }
        return d;
    }

    private static BufferedImage bytesToImg(byte[] d, BufferedImage r) {
        BufferedImage i = new BufferedImage(r.getWidth(), r.getHeight(), BufferedImage.TYPE_INT_RGB);
        int p = 0;
        for (int y = 0; y < i.getHeight(); y++)
            for (int x = 0; x < i.getWidth(); x++)
                i.setRGB(x, y, ((d[p++] & 255) << 16) | ((d[p++] & 255) << 8) | (d[p++] & 255));
        return i;
    }

    private static byte[] read(File f) throws IOException {
        return new FileInputStream(f).readAllBytes();
    }

    private static void write(File f, byte[] d) throws IOException {
        try (FileOutputStream o = new FileOutputStream(f)) { o.write(d); }
    }

    private static byte[] rnd(int n) {
        byte[] b = new byte[n];
        new SecureRandom().nextBytes(b);
        return b;
    }

    private static byte[] concat(byte[]... a) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (byte[] b : a) o.write(b);
        return o.toByteArray();
    }

    /* ================= UI ================= */
    private static void title() {
        println("NEUNIX STEGENGINE");
        println("------------------------------");
    }

    private static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void pause(String m) {
        println(m);
        print("Press Enter...");
        new Scanner(System.in).nextLine();
    }

    private static void require(String... s) {
        for (String x : s) if (x == null) error("Missing argument");
    }

    private static void print(String s) { System.out.print(s); }
    private static void println(String s) { System.out.println(s); }
    private static void success(String s) { println("[✓] " + s); }
    private static void error(String s) { throw new RuntimeException(s); }
}
