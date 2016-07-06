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

import com.intellij.idea.plugin.hybris.common.services.NotificationSender;
import com.intellij.idea.plugin.hybris.common.services.impl.NotificationSenderImpl;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.type.system.common.TSMessages;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.Items;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
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
import com.intellij.util.xml.DomManager;
import com.sun.istack.NotNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class ItemsXMLChangedListener implements ProjectManagerListener {

    private static final String ITEMS_XML_FILE = "-items.xml";
    private static final String ITEM_ROOT_CLASS = "de.hybris.platform.core.model.ItemModel";
    private static final String ENUM_ROOT_CLASS = "de.hybris.platform.core.HybrisEnumValue";
    private static final String ITEM_XML_VALIDATION_GROUP = "Items XML validation group";

    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(ITEM_XML_VALIDATION_GROUP,
                                           NotificationDisplayType.BALLOON, true);

    private static final NotificationSender NOTIFICATIONS = new NotificationSenderImpl(NOTIFICATION_GROUP);

    private static final ItemTypeClassValidation ITEM_TYPE_VALIDATION = new ItemTypeClassValidation();
    private static final EnumTypeClassValidation ENUM_TYPE_VALIDATION = new EnumTypeClassValidation();

    private ItemsPropertiesChangedListener itemsPropertiesListener;

    protected class ItemsPropertiesChangedListener extends VirtualFileAdapter {

        private Project project;

        public ItemsPropertiesChangedListener(final Project project)
        {
            super();
            this.project = project;
        }

        @Override
        public void contentsChanged(VirtualFileEvent event)
        {
            if (!event.getFileName().endsWith(ITEMS_XML_FILE))
            {
                return;
            }
            try
            {

                final DomManager domManager = DomManager.getDomManager(project);

                final PsiManager psiManager = PsiManager.getInstance(project);
                final PsiFile psiFile = psiManager.findFile(event.getFile());

                if (psiFile != null && (psiFile instanceof XmlFile))
                {
                    final Items itemsRootElement = (Items) domManager.getFileElement((XmlFile) psiFile).getRootElement();

                    final Collection<PsiClass> inheritedItemClasses = findAllInheritClasses(project, ITEM_ROOT_CLASS);
                    final Collection<PsiClass> inheritedEnumClasses = findAllInheritClasses(project, ENUM_ROOT_CLASS);

                    final List<EnumType> enumTypeList = itemsRootElement.getEnumTypes().getEnumTypes();
                    final String enumValidationMessage = ENUM_TYPE_VALIDATION.validateGeneratedClasses(enumTypeList, inheritedEnumClasses);
                    NOTIFICATIONS.showWarningMessage(enumValidationMessage );

                    final List<ItemType> itemTypeList = itemsRootElement.getItemTypes().getItemTypes();
                    final String itemsValidationMessage = ITEM_TYPE_VALIDATION.validateGeneratedClasses(itemTypeList, inheritedItemClasses);
                    NOTIFICATIONS.showWarningMessage(itemsValidationMessage );

                    if(StringUtils.isNotEmpty(enumValidationMessage) || StringUtils.isNotEmpty(enumValidationMessage))
                    {
                        NOTIFICATIONS.showWarningMessage(HybrisI18NBundleUtils.message(TSMessages.RUN_ANT_CLEAN_ALL));
                    }

                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private Collection<PsiClass> findAllInheritClasses(@NotNull final Project project,
                                                       @NotNull final String rootClass)
    {
        final PsiClass itemRootClass = JavaPsiFacade.getInstance(project).findClass(
            rootClass, GlobalSearchScope.allScope(project));

        if (null == itemRootClass)
        {
            return CollectionUtils.EMPTY_COLLECTION;
        }

        return ClassInheritorsSearch.search(itemRootClass).findAll();
    }

    @Override
    public void projectOpened(final Project project)
    {
        this.itemsPropertiesListener = new ItemsPropertiesChangedListener(project);
        VirtualFileManager.getInstance().addVirtualFileListener(itemsPropertiesListener);
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
