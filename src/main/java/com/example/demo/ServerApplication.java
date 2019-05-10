package com.example.demo;


import java.util.*;

import javax.servlet.http.HttpServlet;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;


@EnableAutoConfiguration
@RestController
public class ServerApplication extends HttpServlet {

    private HashMap<String, DeviceData> devices;
    private HashMap<Integer, RoomData> rooms;
    private final int[] colors = new int[]{0xff00ff00, 0xff303f9f};

    public ServerApplication() {
        super();
        devices = new HashMap<>();
        rooms = new HashMap<>();
    }

    public String deleteChar(String str) {
        if (str != null && str.length() > 0) {
            if (str.charAt(str.length() - 1) == '"')
                str = str.substring(0, str.length() - 1);
            if (str.charAt(0) == '"')
                str = str.substring(1);
        }
        return str;
    }

    //Добавить девайс
    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public Void putDevice(@RequestPart String device, Integer room, Long date) {
        device = deleteChar(device);
        devices.put(device, new DeviceData(room));
        if (!rooms.containsKey(room)) {
            rooms.put(room, new RoomData());

            rooms.get(room).deviceList.push(device);
        } else {
            if (!rooms.get(room).deviceList.contains(device)) {
                rooms.get(room).deviceList.push(device);
                if (rooms.get(room).deviceList.size() <= colors.length + 1) {
                    devices.get(device).color = colors[rooms.get(room).deviceList.size() - 2];
                }
            }
        }
        if (date != null) {
            rooms.get(room).time = date;
            System.out.println("Время: " + date);
        }
        System.out.println("Получено " + device + " в комнате " + room);
        return null;
    }

    //Добавить координаты
    @RequestMapping(value = "/post/coords", method = RequestMethod.POST)
    public Void putCoords(@RequestPart int room, int x1, int y1, int x2, int y2, int color) {
        int a = Arrays.binarySearch(colors, color);
        devices.get(rooms.get(room).deviceList.get(a)).coords = new Coords(x1, y1, x2, y2);
        System.out.println("Coords: " + x1 + "," + y1 + " " + x2 + "," + y2);
        devices.remove(rooms.get(room).deviceList.get(a));
        /*int n = 0;
        for (String i : devices.keySet()) {
            if (devices.get(i).room == room)
                n++;
        }
        if (n <= 1)
            rooms.remove(room);*/
        return null;
    }

    //Получить координаты
    @RequestMapping("/get/coords/{device}")
    public Coords getCoords(@PathVariable("device") String device) {
        return devices.get(device).coords;
    }

    //Получить время запуска
    @RequestMapping("/get/{device}")
    public Long getTime(@PathVariable("device") String device) {
        if (devices.containsKey(device)) {
            int n = 0;
            final int room = devices.get(device).room;
            final Long date = rooms.get(room).time;
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    //Получить цвет
    @RequestMapping("/get/color/{device}")
    public Integer getColor(@PathVariable("device") String device) {
        return devices.get(device).color;
    }

    //Получение номера комнаты
    @RequestMapping("/get/room")
    public Integer getRoom() {
        if (rooms.size() > 0)
            return rooms.size();
        else
            return 1;
    }

    //Добавить видео
    @RequestMapping(value = "/post/video", method = RequestMethod.POST)
    public Void putVideo(@RequestPart String bytes, int room) {
        rooms.get(room).video = bytes.getBytes();
        System.out.println("Добавлено видео в комнате " + room);
        return null;
    }

    //Получение видео
    @RequestMapping("/get/video/{room}")
    public byte[] getVideo(@PathVariable("room") int room) {
        return rooms.get(room).video;
    }

    //Данные каждого гаджета
    private class DeviceData {
        public Integer color, room;
        public Coords coords;

        public DeviceData(int newRoom) {
            room = newRoom;
        }
    }

    //Данные каждой комнаты
    private class RoomData {
        public LinkedList<String> deviceList;
        public Long time;
        public byte[] video;

        public RoomData() {
            deviceList = new LinkedList<>();
        }
    }

    //Класс координат
    private class Coords {
        public Integer x1, y1, x2, y2;

        public Coords(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }
    }
}