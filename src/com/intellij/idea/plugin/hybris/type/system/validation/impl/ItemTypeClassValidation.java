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

package com.intellij.idea.plugin.hybris.type.system.validation.impl;

import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.Attributes;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.validation.AbstractTSClassesValidation;
import com.sun.istack.NotNull;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class ItemTypeClassValidation extends AbstractTSClassesValidation<ItemType, Attribute> {

    private static final String MODEL_SUFFIX = "Model";
    private static final String FIELD_PREFIX = "_";
    private static final String LOCALIZED_PREFIX = "localized";

    @Override
    public List<Attribute> getItemFields(@NotNull final ItemType item)
    {
        final Attributes attributes = item.getAttributes();
        if(null == attributes)
        {
            return (List<Attribute>)CollectionUtils.EMPTY_COLLECTION;
        }
        return attributes.getAttributes();
    }

    @Override
    public String buildGeneratedClassName(final ItemType item)
    {
        return item.getCode() + MODEL_SUFFIX;
    }

    @Override
    public String buildItemName(final ItemType item)
    {
        return item.getCode().toString();
    }

    @Override
    public String buildPropertyName(final Attribute property)
    {
        final String attributeTypeName = property.getType().toString().toLowerCase();
        if(attributeTypeName.startsWith(LOCALIZED_PREFIX))
        {
            return property.getQualifier().toString();
        }
        return FIELD_PREFIX + property.getQualifier().toString();
    }
}
