package com.example.demo;


import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServlet;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@EnableAutoConfiguration
@RestController
public class ServerApplication extends HttpServlet {

    private HashMap<String, DeviceData> devices;
    private HashMap<Integer, RoomData> rooms;
    private final int[] colors = new int[]{0xff00ff00, 0xff303f9f, 0xffffff00};

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

            rooms.get(room).deviceList.addLast(device);
        } else {
            if (!rooms.get(room).deviceList.contains(device)) {
                rooms.get(room).deviceList.addLast(device);
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
        devices.get(rooms.get(room).deviceList.get(a + 1)).coords = new Coords(x1, y1, x2, y2);
        System.out.println("Coords: " + x1 + "," + y1 + " " + x2 + "," + y2);
        //devices.remove(rooms.get(room).deviceList.get(a));
        return null;
    }


    //Получить координаты
    @RequestMapping("/get/coords/{device}")
    public Coords getCoords(@PathVariable("device") String device) {
        Coords coords = devices.get(device).coords;
        if (coords != null) {
            int n = 0;
            for (String i : devices.keySet()) {
                if (devices.get(i).room.equals(devices.get(device).room))
                    n++;
            }
            if (n <= 1)
                rooms.remove(devices.get(device).room);
        }
        return coords == null ? new Coords(-1, -1, -1, -1) : coords;
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
        return rooms.size() + 1;
    }

    /*//Добавить видео
    @RequestMapping(value = "/post/video", method = RequestMethod.POST)
    public Void putVideo(@RequestPart("room") int room, @RequestBody RequestBody requestBody) {
        rooms.get(room).video = requestBody.toString().getBytes();
        System.out.println("Добавлено видео в комнате " + room);
        return null;
    }*/

    /*//Получение видео
    @RequestMapping("/get/video/{room}")
    public byte[] getVideo(@PathVariable("room") int room) {
        return rooms.get(room).video;
    }*/

    //Получение массива цветов
    @RequestMapping("/get/colors")
    public int[] getColors() {
        return colors;
    }

    //Добавить время запуска видео
    @RequestMapping(value = "/post/startVideo", method = RequestMethod.POST)
    public Void putStartVideo(@RequestPart Integer room, Long date) {
        rooms.get(room).videoStart = date;
        return null;
    }

    //Получить время запуска видео
    @RequestMapping("/get/startVideo/{device}")
    public Long getStartVideo(@PathVariable("device") String device) {
        if (devices.containsKey(device)) {
            int n = 0;
            final int room = devices.get(device).room;
            final Long date = rooms.get(room).videoStart;
            if (date != null) {
                return date;
            }
        }
        return (long) 0;
    }

    @GetMapping(value = "/download/{room}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] getFile(@PathVariable("room") int room) {
        return rooms.get(room).video;
    }

    @PostMapping(value = "/upload")
    public Void uploadVideo(@RequestPart("video") MultipartFile video, @RequestPart("room") int room) {
        System.out.println("Видео " + video.getOriginalFilename()+ " в комнате " + room);
        try {
            rooms.get(room).video = video.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        public Long time, videoStart;
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