package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private UserMapper userMapper;

//    @Override
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {

        List<LocalDate> datelist = new ArrayList<>();
        datelist.add(begin);
        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            datelist.add(begin);
        }
        List<Double> turnOverList = new ArrayList<>();
        for (LocalDate localDate : datelist) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            double v = turnover == null ? 0.0 : turnover;
            turnOverList.add(v);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(datelist, ","))
                .turnoverList(StringUtils.join(turnOverList, ","))
                .build();
    }

//    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> datelist = new ArrayList<>();
        datelist.add(begin);
        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            datelist.add(begin);
        }
//        存放新增用户
        List<Integer> newUserList = new ArrayList<>();
//        存放总用户
        List<Integer> totalList = new ArrayList<>();

        for (LocalDate localDate : datelist) {
            LocalDateTime beginDateTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end",endDateTime);
            Integer totalUser = userMapper.countByMap(map);
            map.put("begin",beginDateTime);
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
            totalList.add(totalUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(datelist,","))
                .totalUserList(StringUtils.join(totalList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

//    @Override


    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist = new ArrayList<>();
        datelist.add(begin);
        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            datelist.add(begin);
        }

        List<Integer> OrderCountList = new ArrayList<>();
        List<Integer> OrdersList = new ArrayList<>();
        for (LocalDate localDate : datelist) {

            LocalDateTime beginDateTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginDateTime, endDateTime, null);
            Integer validorderCount = getOrderCount(beginDateTime, endDateTime, Orders.COMPLETED);

            OrderCountList.add(orderCount);
            OrdersList.add(validorderCount);
        }
        Integer total = OrderCountList.stream().reduce(Integer::sum).get();
        Integer ordervalid = OrdersList.stream().reduce(Integer::sum).get();


        Double orderComplettion = 0.0;
        if (total!=0){orderComplettion   = ordervalid.doubleValue()/total;}

        return OrderReportVO.builder().orderCompletionRate(orderComplettion)
                .orderCountList(StringUtils.join(OrderCountList,","))
                .dateList(StringUtils.join(datelist,","))
                .totalOrderCount(total)
                .validOrderCount(ordervalid)
                .validOrderCountList(StringUtils.join(OrderCountList,","))
                .build();
    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end,Integer status) {
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        Integer result = orderMapper.countByMap(map);
        return result == null ? 0 : result;
    }

//    @Override

    public SalesTop10ReportVO dishStatistics(LocalDate begin, LocalDate end) {

        LocalDateTime beginDateTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginDateTime, endDateTime);
        List<String> list = salesTop10.stream()
                .map(GoodsSalesDTO::getName).collect(Collectors.toList());

        String names = StringUtils.join(list, ",");
        List<Integer> nummber = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numbers = StringUtils.join(nummber, ",");

        return SalesTop10ReportVO.builder()
                .nameList(names)
                .numberList(numbers)
                .build();
    }

//    @Override

    public void export(HttpServletResponse response) {

        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MIN));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间"+dateBegin+"到"+dateEnd);

            XSSFRow row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate localDate = dateBegin.plusDays(1);
                workspaceService.getBusinessData(LocalDateTime.of(localDate, LocalTime.MIN), LocalDateTime.of(localDate, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(localDate.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }



            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
