package hoodles.morphe.extension.mirinae.pro;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class EnableProPatch {
    private static final Pattern JS_BUNDLE_PATTERN = Pattern.compile("/js/(explorer|default-lessons~wikinae)\\.[0-9a-f]+\\.js$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GET_PRO_OR_STAFF_PATTERN = Pattern.compile("\\{(var \\w+;return\"pro\")");

    public static WebResourceResponse patchAppJavascript(WebResourceRequest request) {
        String url = request.getUrl().toString();

        if (!JS_BUNDLE_PATTERN.matcher(url).find()) {
            return null;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            for (Map.Entry<String, String> entry : request.getRequestHeaders().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.connect();
            String jsBody = fetch(connection);

            String modifiedBody = patch(jsBody);

            return new WebResourceResponse(
                    "application/javascript",
                    "UTF-8",
                    connection.getResponseCode(),
                    connection.getResponseMessage(),
                    getHeaders(connection),
                    new ByteArrayInputStream(modifiedBody.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static String fetch(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
        );
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();

        return sb.toString();
    }

    private static Map<String, String> getHeaders(HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                headers.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        return headers;
    }

    private static String patch(String jsBody) {
        return GET_PRO_OR_STAFF_PATTERN.matcher(jsBody).replaceAll("{return true; $1");
    }
}
