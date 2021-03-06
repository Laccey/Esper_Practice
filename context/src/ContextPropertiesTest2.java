/**
 * Created by weizinan on 2017/5/10.
 */
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import java.util.Random;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

class ESB
{

    private int id;
    private int price;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getPrice()
    {
        return price;
    }

    public void setPrice(int price)
    {
        this.price = price;
    }

}

class ContextPropertiesListener2 implements UpdateListener
{

    public void update(EventBean[] newEvents, EventBean[] oldEvents)
    {
        if (newEvents != null)
        {
            EventBean event = newEvents[0];
            System.out.println("context.name " + event.get("name") + ", context.id " + event.get("id") + ", " +
                    "context.key1 " + event.get("key1") + ", context.key2 " + event.get("key2"));
        }
    }
}

public class ContextPropertiesTest2
{
    public static void main(String[] args)
    {
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
        EPAdministrator admin = epService.getEPAdministrator();
        EPRuntime runtime = epService.getEPRuntime();

        // 创建context
        String epl1 = "create context esbtest partition by id,price from ESB";
        // context.id针对不同的esb的price建立一个context，如果事件的price相同，则context.id也相同，即表明事件进入了同一个context
        // 不同事件进入同一个context的条件是context包含的属性前后事件的值必须相同
        String epl2 = "context esbtest select context.id,context.name,context.key1,context.key2 from ESB";

        /*
        // context定义  
        create context esbtest2 partition by id from ESB  
          
        // 每当5个id相同的ESB事件进入时，统计price的总和  
        context esbtest select sum(price) from ESB.win:length_batch(5)  
          
        // 根据不同的id，统计3秒内进入的事件的平均price，且price必须大于10  
        context esbtest select avg(price) from ESB(price>10).win:time(3 sec)
        */

        admin.createEPL(epl1);
        EPStatement state = admin.createEPL(epl2);
        state.addListener(new ContextPropertiesListener2());

        //当事件的price属性值相同时会进入同一个context，从输出可看到当price值相同时，context的id是相同的，说明进入到同一个context
        ESB e1 = new ESB();
        e1.setId(1);
        e1.setPrice(20);
        System.out.println("sendEvent: id=1, price=20");
        runtime.sendEvent(e1);

        ESB e2 = new ESB();
        e2.setId(2);
        e2.setPrice(30);
        System.out.println("sendEvent: id=2, price=30");
        runtime.sendEvent(e2);

        ESB e3 = new ESB();
        e3.setId(1);
        e3.setPrice(20);
        System.out.println("sendEvent: id=1, price=20");
        runtime.sendEvent(e3);

        ESB e4 = new ESB();
        e4.setId(4);
        e4.setPrice(20);
        System.out.println("sendEvent: id=4, price=20");
        runtime.sendEvent(e4);
    }
}