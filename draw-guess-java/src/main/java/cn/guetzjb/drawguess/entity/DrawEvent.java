package cn.guetzjb.drawguess.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DrawEvent<T> {

    private String name; //对应enum

    private T data;
}
