package com.example.ticketmachinedemo.controller;

import com.example.ticketmachinedemo.service.TicketMachineService;
import com.example.ticketmachinedemo.utils.ByteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author: 苏敏
 * @date: 2020/9/11 12:04
 */
@RestController
public class DemoController {

    @Autowired
    private TicketMachineService ticketMachineService;

    @GetMapping(value = "/test/{number}")
    public void test(@PathVariable String number) throws IOException {
        ticketMachineService.openBox(Integer.parseInt(number));
    }

    @GetMapping(value = "/list/{number}")
    public void list(@PathVariable String number) throws IOException, InterruptedException {
        String[] split = number.split(",");
        ticketMachineService.openBoxList(split);
    }

    @GetMapping(value = "/openAll")
    public void test1() throws IOException, InterruptedException {
        ticketMachineService.openAll();
    }
}
