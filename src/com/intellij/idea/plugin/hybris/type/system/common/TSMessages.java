/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.type.system.common;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class TSMessages {

    public static final String RUN_ANT_CLEAN_ALL = "hybris.ts.items.validation.run.ant.clean.all";

    public interface ErrorMessages
    {
        String CLASS_NOT_GENERATED = "hybris.ts.items.validation.class.missed.error";
        String FIELDS_NOT_GENERATED = "hybris.ts.items.validation.field.missed.error";
    }

}
