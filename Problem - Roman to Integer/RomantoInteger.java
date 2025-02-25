class Solution {
    public int romanToInt(String s) {
        char strArr[] = s.toCharArray();
        int len = strArr.length;
        int result = 0;
        for(int i = len - 1;i >= 0; i--)
        {
            if(strArr[i] == 'I')
            {
                result = result + 1;
            }
            if(strArr[i] == 'V')
            {
                if(i>0)
                if(strArr[i - 1] == 'I')
                {
                    result = result + 4;
                    i=i - 1;
                    continue;
                }
                result = result + 5;
            }
            if(strArr[i] == 'X')
            {
                if(i > 0)
                if(strArr[i-1] == 'I')
                {
                    i=i - 1;
                    result = result + 9;
                    continue;
                }
                result = result + 10;
            }
            if(strArr[i] == 'L')
            {
                if(i > 0)
                if(strArr[i-1] == 'X')
                {
                    i=i - 1;
                    result = result + 40;
                    continue;
                }
                result = result + 50;
            }
            if(strArr[i] == 'C')
            {
                if(i > 0)
                if(strArr[i-1] == 'X')
                {
                    i=i-1;
                    result = result + 90;
                    continue;
                }
                result = result + 100;
            }
            if(strArr[i] == 'D')
            {
                if(i > 0)
                if(strArr[i-1] == 'C')
                {
                    i=i-1;
                    result = result + 400;
                    continue;
                }
                result = result + 500;
            }
            if(strArr[i] == 'M')
            {
                if(i > 0)
                if(strArr[i-1] == 'C')
                {
                    i=i-1;
                    result = result + 900;
                    continue;
                }
                result = result + 1000;
            }
        }
        return result;
    }
}