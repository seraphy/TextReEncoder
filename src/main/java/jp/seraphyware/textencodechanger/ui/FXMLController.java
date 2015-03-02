package jp.seraphyware.textencodechanger.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FXMLによるコントローラとSceneを関連づけるためのアノテーション.
 *
 * @author seraphy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FXMLController {

    /**
     * FXMLのパス、省略時はコントローラのクラス名 + 「.fxml」.
     */
    String value() default "";

    /**
     * リソースのパス、省略時はコントローラのクラス名.
     */
    String resource() default "";
}
