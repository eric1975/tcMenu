/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.validation;

/**
 * A helper class with static methods that provides short cuts for commonly used property validators.
 */
public class CannedPropertyValidators {
    /**
     * @return a standard boolean validator with choices for true and false.
     */
    public static BooleanPropertyValidationRules boolValidator() {
        return new BooleanPropertyValidationRules();
    }

    /**
     * @return a validator useful for arduino pins
     */
    public static IntegerPropertyValidationRules pinValidator() {
        return new IntegerPropertyValidationRules(0, 254);
    }

    /**
     * @return a pin validator that allows -1 to indicate optionality
     */
    public static IntegerPropertyValidationRules optPinValidator() {
        return new IntegerPropertyValidationRules(-1, 254);
    }

    /**
     * @param max the largest value
     * @return a zero based unsigned int validator
     */
    public static IntegerPropertyValidationRules uintValidator(int max) {
        return new IntegerPropertyValidationRules(0, max);
    }

    /**
     * @return a validator useful for checking variable names
     */
    public static StringPropertyValidationRules variableValidator() {
        return new StringPropertyValidationRules(true, 32);
    }

    /**
     * @return a general purpose string validator
     */
    public static StringPropertyValidationRules textValidator() {
        return new StringPropertyValidationRules(false, 32);
    }

    /**
     * @return a choices validator based on an enum
     * @param values
     */
    public static <T extends Enum> ChoicesPropertyValidationRules choicesValidator(T[] values) {
        return new ChoicesPropertyValidationRules(values);
    }
}
