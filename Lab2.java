/**
 * main
 */
import java.util.Scanner;
public class main {

    public static void main(String[] args) {
        Scanner cin=new Scanner(System.in);
        System.out.print("Enter size of array:");
        int n=cin.nextInt();
        int[] arr=new int[n];
        System.out.println("Enter array elements:");
        for(int i=0;i<n;i++)
        {
            arr[i]=cin.nextInt();
        }
        System.out.print("Enter the number for which we want to know the occurance:");
        int num=cin.nextInt();
        int cnt=0;
        for(int j=0;j<n;j++)
        {
            if(arr[j]==num)
            {
                cnt++;
            }
        }
        System.out.println("Occurance is:"+cnt);
        
        



    }
}
