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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.NotificationSender;
import com.intellij.idea.plugin.hybris.common.services.impl.NotificationSenderImpl;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.type.system.common.TSMessages;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.Items;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.idea.plugin.hybris.type.system.validation.TSRelationsValidation;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.HashMap;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.ITEMS_XML_FILE;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.ITEM_ROOT_CLASS;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class ItemsXMLChangedListener implements ProjectManagerListener {

    private static final Logger LOG = Logger.getInstance(ItemsXMLChangedListener.class);

    private static final String ITEM_XML_VALIDATION_GROUP = "Items XML validation group";

    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(
        ITEM_XML_VALIDATION_GROUP, NotificationDisplayType.BALLOON, true
    );

    private static final NotificationSender NOTIFICATIONS = new NotificationSenderImpl(NOTIFICATION_GROUP);

    private static final ItemTypeClassValidation ITEM_TYPE_VALIDATION = new ItemTypeClassValidation();
    private static final EnumTypeClassValidation ENUM_TYPE_VALIDATION = new EnumTypeClassValidation();
    private static final TSRelationsValidation RELATIONS_VALIDATION = new DefaultTSRelationValidation();

    private ItemsPropertiesChangedListener itemsPropertiesListener;

    protected class ItemsPropertiesChangedListener extends VirtualFileAdapter {

        private Project project;

        public ItemsPropertiesChangedListener(final Project project) {
            super();
            this.project = project;
        }

        @Override
        public void contentsChanged(@NotNull final VirtualFileEvent event) {
            if (!event.getFileName().endsWith(ITEMS_XML_FILE)) {
                return;
            }

            try {

                final DomManager domManager = DomManager.getDomManager(this.project);

                final PsiManager psiManager = PsiManager.getInstance(this.project);
                final PsiFile psiFile = psiManager.findFile(event.getFile());

                if (psiFile != null && (psiFile instanceof XmlFile)) {
                    final DomFileElement<Items> fileElement = domManager.getFileElement((XmlFile) psiFile, Items.class);
                    if (null == fileElement) {
                        return;
                    }

                    final Items itemsRootElement = fileElement.getRootElement();

                    final Map<String, PsiClass> inheritedItemClasses = ItemsXMLChangedListener.this.findAllInheritClasses(
                        this.project, ITEM_ROOT_CLASS
                    );
                    final Map<String, PsiClass> inheritedEnumClasses = ItemsXMLChangedListener.this.findAllInheritClasses(
                        this.project, HybrisConstants.ENUM_ROOT_CLASS
                    );

                    final List<EnumType> enumTypeList = itemsRootElement.getEnumTypes().getEnumTypes();
                    final String enumValidationMessage = ENUM_TYPE_VALIDATION.validateGeneratedClasses(
                        enumTypeList,
                        inheritedEnumClasses
                    );
                    NOTIFICATIONS.showWarningMessage(enumValidationMessage);

                    final List<ItemType> itemTypeList = itemsRootElement.getItemTypes().getItemTypes();
                    final String itemsValidationMessage = ITEM_TYPE_VALIDATION.validateGeneratedClasses(
                        itemTypeList,
                        inheritedItemClasses
                    );
                    NOTIFICATIONS.showWarningMessage(itemsValidationMessage);

                    final List<Relation> relationsList = itemsRootElement.getRelations().getRelations();
                    final String relationsValidationMessage = RELATIONS_VALIDATION.validateRelations(
                        inheritedItemClasses,
                        relationsList
                    );
                    NOTIFICATIONS.showWarningMessage(relationsValidationMessage);

                    if (StringUtils.isNotEmpty(enumValidationMessage)
                        || StringUtils.isNotEmpty(itemsValidationMessage)
                        || StringUtils.isNotEmpty(relationsValidationMessage)) {
                        NOTIFICATIONS.showWarningMessage(HybrisI18NBundleUtils.message(TSMessages.RUN_ANT_CLEAN_ALL));
                    }
                }
            } catch (Exception e) {
                LOG.error(String.format("Items validation error. File: %s", event.getFileName()), e);
            }
        }
    }


    @NotNull
    private Map<String, PsiClass> findAllInheritClasses(
        @NotNull final Project project,
        @NotNull final String rootClass
    ) {
        Validate.notNull(project);
        Validate.notNull(rootClass);

        final PsiClass itemRootClass = JavaPsiFacade.getInstance(project).findClass(
            rootClass, GlobalSearchScope.allScope(project));

        if (null == itemRootClass) {
            return Collections.emptyMap();
        }

        final Collection<PsiClass> foundClasses = ClassInheritorsSearch.search(itemRootClass).findAll();
        final Map<String, PsiClass> result = new HashMap<>();
        for (final PsiClass psiClass : foundClasses) {
            result.put(psiClass.getName(), psiClass);
        }
        return result;

    }

    @Override
    public void projectOpened(final Project project) {
        this.itemsPropertiesListener = new ItemsPropertiesChangedListener(project);
        VirtualFileManager.getInstance().addVirtualFileListener(this.itemsPropertiesListener);
    }

    @Override
    public boolean canCloseProject(final Project project) {
        return false;
    }

    @Override
    public void projectClosed(final Project project) {
        VirtualFileManager.getInstance().removeVirtualFileListener(this.itemsPropertiesListener);
    }

    @Override
    public void projectClosing(final Project project) {

    }
}
