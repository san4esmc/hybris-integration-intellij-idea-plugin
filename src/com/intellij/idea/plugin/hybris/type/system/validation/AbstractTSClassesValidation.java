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

package com.intellij.idea.plugin.hybris.type.system.validation;

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.type.system.common.TSMessages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public abstract class AbstractTSClassesValidation<T, M> {

    public String validateGeneratedClasses(
        @NotNull final List<T> xmlDefinedTypes,
        @NotNull final Map<String, PsiClass> generatedClasses
    ) {
        Validate.notNull(xmlDefinedTypes);
        Validate.notNull(generatedClasses);

        final List<PsiClass> filteredClasses = this.filterXmlTypesClasses(generatedClasses, xmlDefinedTypes);

        String validationMessage;

        for (final T xmlType : xmlDefinedTypes) {
            final PsiClass generatedClass = this.getGeneratedClassForItem(xmlType, filteredClasses);

            if (null == generatedClass) {
                return HybrisI18NBundleUtils.message(
                    TSMessages.ErrorMessages.CLASS_NOT_GENERATED,
                    this.buildItemName(xmlType)
                );
            }

            validationMessage = this.validateClass(xmlType, generatedClass);
            if (StringUtils.isNotEmpty(validationMessage)) {
                return validationMessage;
            }

        }

        return StringUtils.EMPTY;
    }

    /**
     * Takes all generated classes and filter only types defined in items.xml
     */
    private List<PsiClass> filterXmlTypesClasses(
        @NotNull final Map<String, PsiClass> classesToFilter,
        @NotNull final List<T> itemsToFind
    ) {
        Validate.notNull(classesToFilter);
        Validate.notNull(itemsToFind);

        String modelName;
        final List<PsiClass> filteredItemClasses = new ArrayList<>();
        for (final T item : itemsToFind) {
            modelName = this.buildGeneratedClassName(item);
            filteredItemClasses.add(classesToFilter.get(modelName));

        }

        return filteredItemClasses;
    }

    private String validateClass(
        @NotNull final T xmlType,
        @NotNull final PsiClass generatedClass
    ) {
        Validate.notNull(xmlType);
        Validate.notNull(generatedClass);

        final List<M> itemFields = this.getItemFields(xmlType);

        PsiField fieldToValidate;
        for (final M xmlField : itemFields) {
            fieldToValidate = this.getGeneratedFieldForAttribute(xmlField, generatedClass);

            if (null == fieldToValidate) {
                return HybrisI18NBundleUtils.message(
                    TSMessages.ErrorMessages.FIELDS_NOT_GENERATED,
                    this.buildPropertyName(xmlField),
                    this.buildItemName(xmlType)
                );
            }
        }

        return StringUtils.EMPTY;
    }

    /**
     * Finds field in generated class for attribute defined for type in items.xml
     */
    private PsiField getGeneratedFieldForAttribute(
        @NotNull final M field,
        @NotNull final PsiClass generatedClass
    ) {
        Validate.notNull(field);
        Validate.notNull(generatedClass);

        String filedName;
        for (final PsiField generatedField : generatedClass.getAllFields()) {
            filedName = this.buildPropertyName(field);

            if (generatedField.getName().toLowerCase().endsWith(filedName.toLowerCase())) {
                return generatedField;
            }
        }

        return null;
    }

    /**
     * Finds generated class for type defined in items.xml
     */
    @Nullable
    private PsiClass getGeneratedClassForItem(
        @NotNull final T xmlType,
        @NotNull final List<PsiClass> generatedClasses
    ) {
        Validate.notNull(xmlType);
        Validate.notNull(generatedClasses);

        for (final PsiClass psiClass : generatedClasses) {
            if (psiClass.getName().endsWith(this.buildGeneratedClassName(xmlType))) {
                return psiClass;
            }
        }

        return null;
    }

    public abstract String buildGeneratedClassName(T item);

    public abstract String buildItemName(T item);

    public abstract String buildPropertyName(M property);

    public abstract List<M> getItemFields(@NotNull final T item);
}
