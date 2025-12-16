package sunshine.weather.model;

public enum WeatherCode {
    CLEAR_SKY(new int[]{0}, "맑음"),
    PARTLY_CLOUDY(new int[]{1, 2, 3}, "구름 조금"),
    CLOUDY(new int[]{45, 48}, "흐림"),
    RAIN(new int[]{51, 53, 55, 56, 57, 61, 63, 65, 66, 67}, "비"),
    SNOW(new int[]{71, 73, 75, 77}, "눈"),
    THUNDERSTORM(new int[]{95, 96, 99}, "천둥번개");

    private final int[] codes;
    private final String description;

    WeatherCode(int[] codes, String description) {
        this.codes = codes;
        this.description = description;
    }

    public static String getDescription(int code) {
        return findWeatherByCode(code).description;
    }

    private static WeatherCode findWeatherByCode(int code) {
        for (WeatherCode weather : values()) {
            if (containsCode(weather.codes, code)) {
                return weather;
            }
        }
        return CLEAR_SKY;
    }

    private static boolean containsCode(int[] codes, int targetCode) {
        for (int code : codes) {
            if (code == targetCode) {
                return true;
            }
        }
        return false;
    }
}