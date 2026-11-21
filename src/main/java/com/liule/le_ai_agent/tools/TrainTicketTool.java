package com.liule.le_ai_agent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 火车票查询工具，数据来源：https://api.lolimi.cn/API/hc/api
 */
@Slf4j
@Component
public class TrainTicketTool {

    private static final String QUERY_API_URL = "https://api.lolimi.cn/API/hc/api";

    private static final int TIMEOUT_MILLIS = 30_000;

    /**
     * 接口不同返回格式下可能包裹车次列表的字段名
     */
    private static final String[] TRAIN_LIST_KEYS = {"data", "result", "list", "trains", "rows"};

    @Tool(description = "Query train tickets between two Chinese cities. Returns train numbers, departure and arrival stations and times, and the available seat list for each train")
    public String queryTrainTickets(
            @ToolParam(description = "Departure city name in Chinese, e.g. 北京") String departure,
            @ToolParam(description = "Arrival city name in Chinese, e.g. 上海") String arrival) {

        if (StrUtil.hasBlank(departure, arrival)) {
            return "出发地和目的地不能为空";
        }

        Map<String, Object> params = new HashMap<>();
        params.put("departure", departure);
        params.put("arrival", arrival);
        params.put("type", "json");

        try {
            String response = HttpUtil.get(QUERY_API_URL, params, TIMEOUT_MILLIS);
            JSONArray trains = resolveTrains(response);
            if (trains == null || trains.isEmpty()) {
                return StrUtil.format("未查询到 {} 到 {} 的车次信息", departure, arrival);
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < trains.size(); i++) {
                if (!(trains.get(i) instanceof JSONObject train)) {
                    continue;
                }
                sb.append(sb.isEmpty() ? "" : "\n")
                        .append(i + 1).append(". ")
                        .append(StrUtil.blankToDefault(train.getStr("TrainNumber"), "未知车次"))
                        .append("  ")
                        .append(StrUtil.blankToDefault(train.getStr("start"), departure))
                        .append(" → ")
                        .append(StrUtil.blankToDefault(train.getStr("end"), arrival)).append("\n")
                        .append("   出发时间：").append(StrUtil.blankToDefault(train.getStr("DepartTime"), "未知"))
                        .append(" | 到达时间：").append(StrUtil.blankToDefault(train.getStr("ArriveTime"), "未知")).append("\n")
                        .append("   座位信息：").append(formatSeatList(train.get("SeatList")));
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("火车票查询失败，departure={}, arrival={}", departure, arrival, e);
            return "火车票查询失败，请稍后重试";
        }
    }

    /**
     * 兼容接口可能返回的多种 JSON 结构：顶层数组、data/result 等字段包裹的数组、单条记录对象。
     */
    private JSONArray resolveTrains(String response) {
        String body = StrUtil.trim(response);
        if (StrUtil.isBlank(body)) {
            return null;
        }
        if (JSONUtil.isTypeJSONArray(body)) {
            return JSONUtil.parseArray(body);
        }
        if (!JSONUtil.isTypeJSONObject(body)) {
            return null;
        }

        JSONObject root = JSONUtil.parseObj(body);
        for (String key : TRAIN_LIST_KEYS) {
            Object value = root.get(key);
            if (value instanceof JSONArray array) {
                return array;
            }
            if (value instanceof JSONObject nested) {
                for (String nestedKey : TRAIN_LIST_KEYS) {
                    if (nested.get(nestedKey) instanceof JSONArray array) {
                        return array;
                    }
                }
                if (nested.containsKey("TrainNumber")) {
                    return JSONUtil.parseArray("[" + nested + "]");
                }
            }
        }
        if (root.containsKey("TrainNumber")) {
            return JSONUtil.parseArray("[" + root + "]");
        }
        return null;
    }

    /**
     * 座位信息可能是数组、对象或字符串，这里统一转为可读文本。
     */
    private String formatSeatList(Object seatList) {
        if (seatList == null) {
            return "暂无座位信息";
        }
        if (seatList instanceof JSONArray seats) {
            if (seats.isEmpty()) {
                return "暂无座位信息";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < seats.size(); i++) {
                if (i > 0) {
                    sb.append("；");
                }
                if (seats.get(i) instanceof JSONObject seat) {
                    String name = firstNonBlank(seat, "seatName", "name", "type", "seat");
                    String price = firstNonBlank(seat, "price", "seatPrice");
                    String count = firstNonBlank(seat, "count", "num", "left", "remain");
                    if (name == null && price == null && count == null) {
                        sb.append(seat);
                    } else {
                        sb.append(StrUtil.blankToDefault(name, "座位"));
                        if (price != null) {
                            sb.append(" ").append(price);
                        }
                        if (count != null) {
                            sb.append(" 余票").append(count);
                        }
                    }
                } else {
                    sb.append(seats.get(i));
                }
            }
            return sb.toString();
        }
        return seatList.toString();
    }

    private String firstNonBlank(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.getStr(key);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
