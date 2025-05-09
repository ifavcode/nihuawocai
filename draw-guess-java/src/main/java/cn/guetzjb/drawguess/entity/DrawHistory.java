package cn.guetzjb.drawguess.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DrawHistory {

    private String lineColor;
    private Integer lineWidth;
    private List<Point> points;
}
