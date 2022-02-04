package com.skylerdache.spacepong.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.awt.Color;

@Converter
public class ColorConverter implements AttributeConverter<Color, String> {

    @Override
    public String convertToDatabaseColumn(Color color) {
        return Integer.toString(color.getRGB());
    }

    @Override
    public Color convertToEntityAttribute(String colorS) {
        return new Color(Integer.parseInt(colorS));
    }
}
