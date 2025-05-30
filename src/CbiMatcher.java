import java.util.Map;

public class CbiMatcher {

    public static String findClosestCbiId(String input, Map<String, CbiEntry> cbiMap) {
        input = input.toLowerCase();
        String bestId = "50";
        double bestRatio = 0.0;

        for (Map.Entry<String, CbiEntry> entry : cbiMap.entrySet()) {
            String description = entry.getValue().description.toLowerCase();
            double similarity = similarity(input, description);


            if (similarity > bestRatio) {
                bestRatio = similarity;
                bestId = entry.getKey();
            }

        }
        if (bestId.contains(" ")) {
            bestId = bestId.replace(" ", "");
        }

        return "\"" + bestId;

    }


    public static String extractInitialMeaningfulPart(String input) {
        String[] tokens = input.split("\\s+");
        StringBuilder builder = new StringBuilder();
        int wordsAdded = 0;

        for (String token : tokens) {

            String cleaned = token.replaceAll("^[^\\p{L}\\p{M}']+|[^\\p{L}\\p{M}']+$", "");

            if (cleaned.matches(".*\\d.*")) continue;

            if (!cleaned.matches(".*[a-zA-ZàèéìòùÀÈÉÌÒÙ].*")) continue;

            builder.append(cleaned.toLowerCase()).append(" ");
            wordsAdded++;

            if (wordsAdded >= 4) break;
        }

        return builder.toString().trim();
    }


    private static double similarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    public static String findClosestDescription(Map<String, CbiEntry> cbiMap,String key) {

        String description= "";
        for (Map.Entry<String, CbiEntry> entry : cbiMap.entrySet()) {

            if(key.equals("\""+entry.getKey())) {
                description = entry.getValue().description;
                break;
            }
        }

        return "\" " + description;
    }

}


