///*
// * Copyright 2019 xincao9@gmail.com.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.github.xincao9.jswitcher.cli;
//
//import com.alibaba.fastjson.JSON;
//import com.github.xincao9.jsonrpc.core.client.JsonRPCClient;
//import com.github.xincao9.jsonrpc.core.common.Request;
//import com.github.xincao9.jsonrpc.core.common.Response;
//import com.github.xincao9.jswitcher.api.service.SwitcherService;
//import com.github.xincao9.jswitcher.api.vo.Switcher;
//import java.util.ArrayList;
//import java.util.List;
//import org.jline.reader.Completer;
//import org.jline.reader.EndOfFileException;
//import org.jline.reader.LineReader;
//import org.jline.reader.LineReaderBuilder;
//import org.jline.reader.UserInterruptException;
//import org.jline.reader.impl.completer.ArgumentCompleter;
//import org.jline.reader.impl.completer.NullCompleter;
//import org.jline.reader.impl.completer.StringsCompleter;
//import org.jline.reader.impl.history.DefaultHistory;
//import org.jline.terminal.Terminal;
//import org.jline.terminal.TerminalBuilder;
//
///**
// *  开关客户端
// * 
// * @author xincao9@gmail.com
// */
//public class JswitcherCli {
//
//    /**
//     * 入口方法
//     * 
//     * @param args 参数
//     * @throws Throwable 异常
//     */
//    public static void main(String... args) throws Throwable {
//        Terminal terminal = TerminalBuilder.builder()
//                .system(true)
//                .build();
//        Completer completer = new ArgumentCompleter(new StringsCompleter("connect", "check", "on", "off", "set", "list", "quit"), NullCompleter.INSTANCE);
//        LineReader lineReader = LineReaderBuilder.builder()
//                .terminal(terminal)
//                .completer(completer)
//                .history(new DefaultHistory())
//                .build();
//        String prompt = "jswitcher $>";
//        JsonRPCClient jsonRPCClient = JsonRPCClient.defaultJsonRPCClient();
//        jsonRPCClient.start();
//        SwitcherService switcherService = jsonRPCClient.proxy(SwitcherService.class);
//        String host = "127.0.0.1";
//        short port = 12306;
//        while (true) {
//            try {
//                String line = lineReader.readLine(prompt);
//                String[] ss = line.split("\\s");
//                List<Object> params = new ArrayList();
//                if (ss == null || ss.length == 0) {
//                    continue;
//                }
//                String cmd = ss[0];
//                if (ss.length == 1 && any(cmd, "list", "quit")) {
//                    if ("list".equalsIgnoreCase(cmd)) {
//                        List<Switcher> switchers = switcherService.list();
//                        System.out.println(JSON.toJSONString(switchers, true));
//                    } else if ("quit".equalsIgnoreCase(cmd)) {
//                        jsonRPCClient.shutdown();
//                        return;
//                    }
//                } else if (ss.length == 2 && any(cmd, "check", "on", "off")) {
//                    params.add(ss[1]);
//                    Request request = createRequest(host, port, cmd, params);
//                    if ("check".equalsIgnoreCase(cmd)) {
//                        Response<Boolean> response = jsonRPCClient.invoke(request);
//                        switcherService.check(cmd);
//                        System.out.println(JSON.toJSONString(response, true));
//                    } else {
//                        jsonRPCClient.invoke(request);
//                    }
//                } else if (ss.length == 3 && any(cmd, "connect", "set")) {
//                    if ("connect".equalsIgnoreCase(cmd)) {
//                        host = ss[1];
//                        port = Short.valueOf(ss[2]);
//                    } else {
//                        params.add(ss[1]);
//                        params.add(Boolean.valueOf(ss[2]));
//                        Request request = createRequest(host, port, "set", params);
//                        jsonRPCClient.invoke(request);
//                    }
//                }
//            } catch (UserInterruptException e) {
//                jsonRPCClient.shutdown();
//                return;
//            } catch (EndOfFileException e) {
//                jsonRPCClient.shutdown();
//                return;
//            } catch (Throwable e) {
//            }
//        }
//    }
//
//    private static boolean any(String cmd, String... cs) {
//        for (String s : cs) {
//            if (cmd.equalsIgnoreCase(s)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//}
