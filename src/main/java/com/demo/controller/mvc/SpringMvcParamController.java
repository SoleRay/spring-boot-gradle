package com.demo.controller.mvc;


import com.demo.dto.request.array.EntityArrayInEntity;
import com.demo.dto.request.array.SimpleArrayInEntity;
import com.demo.dto.request.collection.EntityListInEntity;
import com.demo.dto.request.collection.SimpleListInEntity;
import com.demo.dto.request.demo.DemoReqDTO;
import com.demo.dto.request.entity.*;
import com.demo.dto.request.map.EntityMapInEntity;
import com.demo.dto.request.map.SimpleMapInEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mvc/param")
public class SpringMvcParamController {

    /**
     * id = 1
     * str = "bob"
     */
    @PostMapping(value="/doSimpleProperty")
    public void doSimpleProperty(Integer id, @RequestParam String str){
    }

    /**
     * array = "a,b,c"
     */
    @PostMapping(value="/doSimpleArray")
    public void doSimpleArray(String[] array){
    }

    /**
     * array[0] = "a"
     * array[1] = "b"
     */
    @PostMapping(value="/doSimpleArrayInEntity")
    public void doSimpleArrayInEntity(SimpleArrayInEntity simpleArrayInEntity){
    }

//    @PostMapping(value="/doEntityArray")
//    public Result doEntityArray(Entity[] array){
//        return Result.success();
//    }

    /**
     * entities[0].id = 1
     * entities[0].name = "bob"
     * entities[1].id = 2
     * entities[1].name = "john"
     */
    @PostMapping(value="/doEntityArrayInEntity")
    public void doEntityArrayInEntity(EntityArrayInEntity entity){
    }

    /**
     * stringList = "a,b,c"
     */
    @PostMapping(value="/doSimpleList")
    public void doSimpleList(/*@ModelAttribute(name = "nameList") */List<String> list){
    }

    /**
     * list[0] = "a"
     * list[1] = "b"
     */
    @PostMapping(value="/doSimpleListInEntity")
    public void doSimpleListInEntity(SimpleListInEntity entity){
    }

    /**
     * entityList[0].id = 1
     * entityList[0].name = "bob"
     * entityList[1].id = 2
     * entityList[1].name = "john"
     */
    @PostMapping(value="/doEntityListInEntity")
    public void doEntityListInEntity(EntityListInEntity entity){
    }

    /**
     * id = 1
     * name = "bob"
     */
    @PostMapping(value="/doSimpleMap")
    public void doSimpleMap(@RequestParam Map<String,String> map){
    }


    /**
     * map[id] = 1
     * map[name] = "bob"
     */
    @PostMapping(value="/doSimpleMapInEntity")
    public void doSimpleMapInEntity(SimpleMapInEntity entity){
    }

    /**
     * map[car].id = 1
     * map[car].name = "bob"
     * map[fruit].id = 1
     * map[fruit].name = "apple"
     */
    @PostMapping(value="/doEntityMapInEntity")
    public void doEntityMapInEntity(EntityMapInEntity entity){
    }

    /**
     * id = 1
     * name = "bob"
     * createDate = "2020-02-01"
     */
    @PostMapping(value="/doEntity")
    public void doEntity(Entity entity) {
    }

    /**
     * id = 1
     * name = "bob"
     * entity.id = "2"
     * entity.name = "dog"
     */
    @PostMapping(value="/doEntityInEntity")
    public void doEntityInEntity(EntityInEntity entity) {
    }

    /**
     * {
     * 	"id":"1",
     * 	"name":"bob",
     * 	"createDate":"2020-02-01"
     * }
     */
    @PostMapping(value="/doJsonEntity",produces= MediaType.APPLICATION_JSON_VALUE)
    public void doJsonEntity(@RequestBody Entity entity) {
    }


    @PostMapping(value="/doMix",produces= MediaType.APPLICATION_JSON_VALUE)
    public void doMix(@RequestBody DemoReqDTO param,
                        @RequestParam Map map,
                        @RequestHeader(value = "token", required = false) String token,
                        String string) {
    }


    @PostMapping(value="/doRequstAndResponse")
    public void doRequstAndResponse(HttpServletRequest request, HttpServletResponse response){

    }

//    @ModelAttribute
//    public void preModel(int id, String name, Model model){
//        model.addAttribute(id);
//        model.addAttribute(name);
//    }
    @ModelAttribute
    public void doModelMap(ModelMap modelMap){

    }

}
