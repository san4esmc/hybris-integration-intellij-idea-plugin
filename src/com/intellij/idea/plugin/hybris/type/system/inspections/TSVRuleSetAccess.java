/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2016 Viatra2 s.r.o <viatra2@gmail.com>
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

package com.intellij.idea.plugin.hybris.type.system.inspections;

import com.hybris.ps.tsv.main.TSVMain;
import com.hybris.ps.tsv.rules.IRule;
import com.hybris.ps.tsv.rules.IRuleSet;
import com.hybris.ps.tsv.rules.executable.XPathRule;
import com.hybris.ps.tsv.services.IRuleService;
import com.intellij.openapi.components.ServiceManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TSVRuleSetAccess {

    private static final String DEFAULT_RULES_FILE = "ruleset.xml";
    private final IRuleSet myRuleSet;
    private List<XPathRule> myXpathRules;

    public static TSVRuleSetAccess getInstance() {
        return ServiceManager.getService(TSVRuleSetAccess.class);
    }

    public TSVRuleSetAccess() {
        final ClassLoader pluginClassLoader = getClass().getClassLoader();

        final ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
        appContext.setClassLoader(pluginClassLoader);
        appContext.setConfigLocation("tsv-spring-config.xml"); //see resources
        appContext.refresh();

        final ClassLoader oldContextCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            final TSVMain tsvMain = (TSVMain) appContext.getBean("tsvMain");
            final IRuleService ruleService = tsvMain.getRuleService();

            myRuleSet = ruleService.loadRulesFromStream(pluginClassLoader.getResourceAsStream(DEFAULT_RULES_FILE));
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextCL);
        }
    }

    public IRuleSet getAllRules() {
        return myRuleSet;
    }

    public List<XPathRule> getXPathRules() {
        if (myXpathRules == null) {
            myXpathRules = new ArrayList<>(myRuleSet.getRules().size());
            for (IRule next : myRuleSet.getRules()) {
                if (next instanceof XPathRule) {
                    myXpathRules.add((XPathRule) next);
                }
            }
        }
        return myXpathRules;
    }

}