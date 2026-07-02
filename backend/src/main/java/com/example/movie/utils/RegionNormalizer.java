package com.example.movie.utils;

import java.util.Map;

public final class RegionNormalizer {

    private static final Map<String, String> MAP = Map.ofEntries(
        Map.entry("China", "中国"),
        Map.entry("United States of America", "美国"),
        Map.entry("United States", "美国"),
        Map.entry("USA", "美国"),
        Map.entry("Japan", "日本"),
        Map.entry("South Korea", "韩国"),
        Map.entry("Korea", "韩国"),
        Map.entry("Korea, Republic of", "韩国"),
        Map.entry("United Kingdom", "英国"),
        Map.entry("UK", "英国"),
        Map.entry("Great Britain", "英国"),
        Map.entry("France", "法国"),
        Map.entry("Germany", "德国"),
        Map.entry("Italy", "意大利"),
        Map.entry("Canada", "加拿大"),
        Map.entry("Australia", "澳大利亚"),
        Map.entry("India", "印度"),
        Map.entry("Spain", "西班牙"),
        Map.entry("Brazil", "巴西"),
        Map.entry("Russia", "俄罗斯"),
        Map.entry("Russian Federation", "俄罗斯"),
        Map.entry("Mexico", "墨西哥"),
        Map.entry("Sweden", "瑞典"),
        Map.entry("Denmark", "丹麦"),
        Map.entry("Norway", "挪威"),
        Map.entry("Finland", "芬兰"),
        Map.entry("Netherlands", "荷兰"),
        Map.entry("Belgium", "比利时"),
        Map.entry("Poland", "波兰"),
        Map.entry("Turkey", "土耳其"),
        Map.entry("Thailand", "泰国"),
        Map.entry("Argentina", "阿根廷"),
        Map.entry("New Zealand", "新西兰"),
        Map.entry("Ireland", "爱尔兰"),
        Map.entry("Portugal", "葡萄牙"),
        Map.entry("Greece", "希腊"),
        Map.entry("Switzerland", "瑞士"),
        Map.entry("Austria", "奥地利"),
        Map.entry("Czech Republic", "捷克"),
        Map.entry("Hungary", "匈牙利"),
        Map.entry("Romania", "罗马尼亚"),
        Map.entry("South Africa", "南非"),
        Map.entry("Egypt", "埃及"),
        Map.entry("Iran", "伊朗"),
        Map.entry("Israel", "以色列"),
        Map.entry("Singapore", "新加坡"),
        Map.entry("Malaysia", "马来西亚"),
        Map.entry("Indonesia", "印度尼西亚"),
        Map.entry("Philippines", "菲律宾"),
        Map.entry("Vietnam", "越南"),
        Map.entry("United Arab Emirates", "阿联酋"),
        Map.entry("Saudi Arabia", "沙特阿拉伯"),
        Map.entry("Chile", "智利"),
        Map.entry("Colombia", "哥伦比亚"),
        Map.entry("Peru", "秘鲁"),
        Map.entry("Morocco", "摩洛哥"),
        Map.entry("Nigeria", "尼日利亚"),
        Map.entry("Kenya", "肯尼亚"),
        Map.entry("Iceland", "冰岛"),
        Map.entry("Luxembourg", "卢森堡"),
        Map.entry("Ukraine", "乌克兰"),
        Map.entry("Croatia", "克罗地亚"),
        Map.entry("Serbia", "塞尔维亚"),
        Map.entry("Bulgaria", "保加利亚"),
        Map.entry("Slovakia", "斯洛伐克"),
        Map.entry("Slovenia", "斯洛文尼亚"),
        Map.entry("Estonia", "爱沙尼亚"),
        Map.entry("Latvia", "拉脱维亚"),
        Map.entry("Lithuania", "立陶宛"),
        Map.entry("Taiwan", "中国台湾"),
        Map.entry("Hong Kong", "中国香港"),
        Map.entry("Macau", "中国澳门"),
        Map.entry("Hong Kong SAR China", "中国香港"),
        Map.entry("Palestine", "巴勒斯坦")
    );

    private RegionNormalizer() {}

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        return MAP.getOrDefault(trimmed, trimmed);
    }
}
