package playground.solrmarc.index.utils;

import java.io.IOException;

public class StringReader extends java.io.Reader
{
    protected final String string;
    protected int index = 0;
    protected int mark = 0;

    public StringReader(String string)
    {
        this.string = string;
    }

    /**
     * Reads one char from the source string.
     *
     * @return one char of the source string.
     */
    public int read()
    {
        return string.charAt(index++);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        if (length() == 0)
        {
            return (-1);
        }
        if (len > length())
        {
            len = string.length();
        }
        string.getChars(index, index + len, cbuf, off);
        index += len;
        return (len);
    }

    /**
     * Reads a substring of given length.
     *
     * @param length
     *            the length of the substring.
     * @return the substring.
     * @throws IndexOutOfBoundsException
     *             if the length is larger than the length of the source string,
     *             or the length is negative.
     */
    public String readString(final int length)
    {
        return string.substring(index, index += length);
    }

    public String readStringUntil(final char c)
    {
        return readString(indexOf(c));
    }

    public String readStringUntilUnless(final char c)
    {
        final int ind = indexOf(c);
        return (ind > -1) ? readString(ind) : readAll();
    }

    /**
     * Reads a substring of the source string.
     * <p/>
     * The data between the current position and beginIndex will be ignored.
     *
     * @param beginIndex
     *            the beginning index, inclusive.
     * @param endIndex
     *            the ending index, exclusive.
     * @return the substring.
     * @throws IndexOutOfBoundsException
     *             - if the beginIndex is negative, or endIndex is larger than
     *             the length of the source string, or beginIndex is larger than
     *             endIndex.
     */
    public String readString(final int beginIndex, final int endIndex)
    {
        skip(beginIndex);
        return readString(endIndex - beginIndex);
    }

    /**
     * After each reading operation this value decreases.
     *
     * @return The remaining length of the source string.
     */
    public int length()
    {
        return string.length() - index;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character.
     *
     * @param c
     *            the needle
     * @return the index of the first occurrence of the character in the
     *         character sequence represented by this object, or a negative
     *         number, if the character does not occur.
     */
    public int indexOf(final char c)
    {
        return string.indexOf(c, index) - index;
    }

    /**
     * @param chars
     *            the needles.
     * @return the index of the first occurrence of a char or a negative number,
     *         if the characters do not occur.
     */
    public int indexOfFirst(final char... chars)
    {
        int minIndex = Integer.MAX_VALUE;
        for (final char aChar : chars)
        {
            final int index = indexOf(aChar);
            if (index >= 0)
            {
                minIndex = Math.min(minIndex, index);
            }
        }
        if (minIndex == Integer.MAX_VALUE)
        {
            return -1;
        }
        else
        {
            return minIndex;
        }
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified character.
     *
     * @param c
     *            the needle.
     * @return the index of the last occurrence of the character in the
     *         character sequence represented by this object, or -1 if the
     *         character does not occur.
     */
    public int lastIndexOf(final char c)
    {
        return string.lastIndexOf(c) - index;
    }

    /**
     * Returns true if, and only if, length() is 0.
     *
     * @return if length() is 0, otherwise false.
     */
    public boolean isEmpty()
    {
        return index >= string.length();
    }

    /**
     * Skips until the first occurrence of char c. The char c will not be
     * skipped.
     * <p/>
     * If the char is not present, nothing will happen.
     *
     * @param c
     *            the needle.
     */
    public void skipUntil(final char c)
    {
        final int next = indexOf(c);
        if (next > 0)
        {
            skip(next);
        }
    }

    /**
     * Skips until the char after the first occurrence of char c. The char c
     * will be skipped.
     * <p/>
     * If the char is not present, nothing will happen.
     *
     * @param c
     *            the needle.
     */
    public void skipUntilAfter(final char c)
    {
        final int next = indexOf(c);
        if (next >= 0)
        {
            skip(next + 1);
        }
    }

    public void skip(final int skipCount)
    {
        index += skipCount;
    }

    /**
     * Consumes the remaining string. After this the consumable string is empty.
     *
     * @return the remaining string.
     */
    public String readAll()
    {
        final String all = string.substring(index);
        index = string.length();
        return all;
    }

    /**
     * Marks the current position. A subsequent call to the reset method
     * repositions this stream at the last marked position so that subsequent
     * reads re-read the same bytes.
     */
    public void mark()
    {
        this.mark = index;
    }

    /**
     * Repositions this stream to the position at the time the mark method was
     * last called. If this this stream wasn't marked before, it will reposition
     * to the beginning of this stream. The mark will be reset to the beginning
     * of this stream.
     */
    public void reset()
    {
        this.index = this.mark;
        this.mark = 0;
    }

    /**
     * @return the remaining string without changing the reading position.
     */
    public String getLookahead()
    {
        return string.substring(index);
    }

    public char charAt(int index)
    {
        return string.charAt(this.index + index);
    }

    @Override
    public String toString()
    {
        return super.toString() + ", index: " + index + ", string: " + string + ";";
    }

    @Override
    public void close() throws IOException
    {
        // TODO Auto-generated method stub

    }
}
