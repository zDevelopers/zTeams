/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.zcraft.zteams;

import fr.zcraft.zteams.texts.TextUtils;
import org.junit.Assert;
import org.junit.Test;


public class TextUtilsTest
{
    @Test
    public void testQuotedArgsExtractor()
    {
        Assert.assertArrayEquals(
                "Args without quotes in them should be left as-is",
                TextUtils.extractArgsWithQuotes("this should be left as is".split(" "), 0),
                new String[] {"this", "should", "be", "left", "as", "is"}
        );

        Assert.assertArrayEquals(
                "Quoted parts should be extracted",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account inside a quoted part",
                new String[] {"this", "should", "be", "extracted and \"separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and \\\"separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account inside a quoted part around spaces",
                new String[] {"this", "should", "be", "extracted and \" separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and \\\" separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account outside a quoted part",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this\"", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and separated\" and this\\\" left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account outside a quoted part around spaces",
                new String[] {"this", "should", "\"", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should \\\" be \"extracted and separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "In quoted parts, spaces should be left as is even if multiple",
                new String[] {"this", "should", "be", "extracted   and  separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should be \"extracted   and  separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Outside quoted parts, spaces should be a single separator even if multiple",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this  should   be \"extracted and separated\" and this      left as  is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Quotes should work the same at the beginning of the args",
                new String[] {"this should be extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("\"this should be extracted and separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the beginning should work",
                new String[] {"\"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("\\\"this should be \"extracted and separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the beginning should work even if alone",
                new String[] {"\"", "this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("\\\" this should be \"extracted and separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the end should work",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is\""},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and separated\" and this left as is\\\"".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the end should work even if alone",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is", "\""},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and separated\" and this left as is \\\"".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escape character should be left as-is if before a non-quote character",
                new String[] {"this", "shoul\\d", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this shoul\\d be \"extracted and separated\" and this left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escape character should be left as-is if before a space",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this\\", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and separated\" and this\\ left as is".split(" "), 0)
        );

        Assert.assertArrayEquals(
                "Escape character should be left as-is if alone",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "\\", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("this should be \"extracted and separated\" and this \\ left as is".split(" "), 0)
        );
    }

    @Test
    public void testQuotedArgsExtractorStartingAtAnotherIndex()
    {
        Assert.assertArrayEquals(
                "Args without quotes in them should be left as-is",
                TextUtils.extractArgsWithQuotes("not this but this should be left as is".split(" "), 3),
                new String[] {"this", "should", "be", "left", "as", "is"}
        );

        Assert.assertArrayEquals(
                "Quoted parts should be extracted",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account inside a quoted part",
                new String[] {"this", "should", "be", "extracted and \"separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and \\\"separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account inside a quoted part around spaces",
                new String[] {"this", "should", "be", "extracted and \" separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and \\\" separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account outside a quoted part",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this\"", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and separated\" and this\\\" left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes should not be taken into account outside a quoted part around spaces",
                new String[] {"this", "should", "\"", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should \\\" be \"extracted and separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "In quoted parts, spaces should be left as is even if multiple",
                new String[] {"this", "should", "be", "extracted   and  separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted   and  separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Outside quoted parts, spaces should be a single separator even if multiple",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this  should   be \"extracted and separated\" and this      left as  is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Quotes should work the same at the beginning of the args",
                new String[] {"this should be extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but \"this should be extracted and separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the beginning should work",
                new String[] {"\"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but \\\"this should be \"extracted and separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the beginning should work even if alone",
                new String[] {"\"", "this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but \\\" this should be \"extracted and separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the end should work",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is\""},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and separated\" and this left as is\\\"".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escaped quotes at the end should work even if alone",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "left", "as", "is", "\""},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and separated\" and this left as is \\\"".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escape character should be left as-is if before a non-quote character",
                new String[] {"this", "shoul\\d", "be", "extracted and separated", "and", "this", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this shoul\\d be \"extracted and separated\" and this left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escape character should be left as-is if before a space",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this\\", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and separated\" and this\\ left as is".split(" "), 3)
        );

        Assert.assertArrayEquals(
                "Escape character should be left as-is if alone",
                new String[] {"this", "should", "be", "extracted and separated", "and", "this", "\\", "left", "as", "is"},
                TextUtils.extractArgsWithQuotes("not this but this should be \"extracted and separated\" and this \\ left as is".split(" "), 3)
        );
    }
}
