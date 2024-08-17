package card.uz.cards.entity;

import java.util.Random;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println(generateCustomUUID());
    }
    public static UUID generateCustomUUID() {
        Random random = new Random();
        StringBuilder uuidBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int segment = 1000 + random.nextInt(9000); // 1000 dan 9999 gacha tasodifiy son
            uuidBuilder.append(segment);
            if (i < 3) {
                uuidBuilder.append(" "); // Bo'shliq qo'shish
            }
        }
        String customUUID = uuidBuilder.toString().replace(" ", ""); // Bo'shliqlarni olib tashlaymiz
        return UUID.nameUUIDFromBytes(customUUID.getBytes());
    }
}
