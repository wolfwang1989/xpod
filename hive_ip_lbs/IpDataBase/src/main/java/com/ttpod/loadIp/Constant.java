package com.ttpod.loadIp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by wolf on 14-5-11.
 */
public class Constant {
    static  final String IP_TABLE_NAME = "ip_table";
    static  final String RE_CHINA_PROVINCE  = "(海南|广西|云南|西藏|广东|江西|贵州|福建|湖南|浙江|江苏|安徽|湖北|四川|青海|甘肃|陕西|河南|山东|河北|宁夏|新疆|山西|内蒙古|辽宁|吉林|黑龙江|重庆|上海|北京|天津|台湾|香港|澳门)";
    
    static  final String PROVINCE_CITYS = "{\"北京\":\"海淀区,东城区,西城区,宣武区,丰台区,朝阳区,崇文区,大兴区,石景山区,门头沟区,房山区,通州区,顺义区,怀柔区,昌平区,平谷区,密云县,延庆县\",\"天津\":\"和平区,河西区,河北区,河东区,南开区,红桥区,北辰区,津南区,武清区,塘沽区,西青区,汉沽区,大港区,宝坻区,东丽区,蓟县,静海县,宁河县\",\"上海\":\"黄浦区,卢湾区,徐汇区,长宁区,静安区,普陀区,闸北区,杨浦区,虹口区,闵行区,宝山区,嘉定区,浦东新区,金山区,松江区,青浦区,南汇区,奉贤区,崇明县\",\"重庆\":\"渝中区,大渡口区,江北区,沙坪坝区,九龙坡区,南岸区,北碚区,万盛区,双桥区,渝北区,巴南区,万州区,涪陵区,黔江区,长寿区,江津区,永川区,南川区,綦江县,潼南县,铜梁县,大足县,荣昌县,璧山县,垫江县,武隆县,丰都县,城口县,梁平县,开县,巫溪县,巫山县,奉节县,云阳县,忠县,石柱土家族自治县,彭水苗族土家族自治县,酉阳苗族自治县,秀山土家族苗族自治县\",\"新疆\":{\"地级\":\"乌鲁木齐,克拉玛依\",\"县级\":\"石河子,阿拉尔,图木舒克,五家渠,哈密,吐鲁番,阿克苏,喀什,和田,伊宁,塔城,阿勒泰,奎屯,博乐,昌吉,阜康,库尔勒,阿图什,乌苏\"},\"西藏\":{\"地级\":\"拉萨\",	\"县级\":\"日喀则\"},\"宁夏\":{\"地级\":\"银川,石嘴山,吴忠,固原,中卫\",\"县级\":\"青铜峡,灵武\"},\"内蒙古\":{\"地级\":\"呼和浩特,包头,乌海,赤峰,通辽,鄂尔多斯,呼伦贝尔,巴彦淖尔,乌兰察布\",\"县级\":\"霍林郭勒,满洲里,牙克石,扎兰屯,根河,额尔古纳,丰镇,锡林浩特,二连浩特,乌兰浩特,阿尔山\"},\"广西\":{\"地级\":\"南宁,柳州,桂林,梧州,北海,崇左,来宾,贺州,玉林,百色,河池,钦州,防城港,贵港\",	\"县级\":\"岑溪,凭祥,合山,北流,宜州,东兴,桂平\"},\"黑龙江\":{\"地级\":\"哈尔滨,大庆,齐齐哈尔,佳木斯,鸡西,鹤岗,双鸭山,牡丹江,伊春,七台河,黑河,绥化\",\"县级\":\"五常,双城,尚志,纳河,虎林,密山,铁力,同江,富锦,绥芬河,海林,宁安,穆林,北安,五大连池,肇东,海伦,安达\"},\"吉林\":{\"地级\":\"长春,吉林,四平,辽源,通化,白山,松原,白城\",\"县级\":\"九台,榆树,德惠,舒兰,桦甸,蛟河,磐石,公主岭,双辽,梅河口,集安,临江,大安,洮南,延吉,图们,敦化,龙井,珲春,和龙\"},\"辽宁\":{\"地级\":\"沈阳,大连,鞍山,抚顺,本溪,丹东,锦州,营口,阜新,辽阳,盘锦,铁岭,朝阳,葫芦岛\",\"县级\":\"新民,瓦房店,普兰,庄河,海城,东港,凤城,凌海,北镇,大石桥,盖州,灯塔,调兵山,开原,凌源,北票,兴城\"},\"河北\":{\"地级\":\"石家庄,唐山,邯郸,秦皇岛,保定,张家口,承德,廊坊,沧州,衡水,邢台\",\"县级\":\"辛集,藁城,晋州,新乐,鹿泉,遵化,迁安,武安,南宫,沙河,涿州,定州,安国,高碑店,泊头,任丘,黄骅,河间,霸州,三河,冀州,深州\"},\"山东\":{\"地级\":\"济南,青岛,淄博,枣庄,东营,烟台,潍坊,济宁,泰安,威海,日照,莱芜,临沂,德州,聊城,菏泽,滨州\",\"县级\":\"章丘,胶南,胶州,平度,莱西,即墨,滕州,龙口,莱阳,莱州,招远,蓬莱,栖霞,海阳,青州,诸城,安丘,高密,昌邑,兖州,曲阜,邹城,乳山,文登,荣成,乐陵,临清,禹城\"},\"江苏\":{\"地级\":\"南京,镇江,常州,无锡,苏州,徐州,连云港,淮安,盐城,扬州,泰州,南通,宿迁\",\"县级\":\"江阴,宜兴,邳州,新沂,金坛,溧阳,常熟,张家港,太仓,昆山,吴江,如皋,通州,海门,启东,东台,大丰,高邮,江都,仪征,丹阳,扬中,句容,泰兴,姜堰,靖江,兴化\"},\"安徽\":{\"地级\":\"合肥,蚌埠,芜湖,淮南,亳州,阜阳,淮北,宿州,滁州,安庆,巢湖,马鞍山,宣城,黄山,池州,铜陵\",\"县级\":\"界首,天长,明光,桐城,宁国\"},\"浙江\":{\"地级\":\"杭州,嘉兴,湖州,宁波,金华,温州,丽水,绍兴,衢州,舟山,台州\",\"县级\":\"建德,富阳,临安,余姚,慈溪,奉化,瑞安,乐清,海宁,平湖,桐乡,诸暨,上虞,嵊州,兰溪,义乌,东阳,永康,江山,临海,温岭,龙泉\"},\"福建\":{\"地级\":\"福州,厦门,泉州,三明,南平,漳州,莆田,宁德,龙岩\",\"县级\":\"福清,长乐,永安,石狮,晋江,南安,龙海,邵武,武夷山,建瓯,建阳,漳平,福安,福鼎\"},\"广东\":{\"地级\":\"广州,深圳,汕头,惠州,珠海,揭阳,佛山,河源,阳江,茂名,湛江,梅州,肇庆,韶关,潮州,东莞,中山,清远,江门,汕尾,云浮\",\"县级\":\"增城,从化,乐昌,南雄,台山,开平,鹤山,恩平,廉江,雷州,吴川,高州,化州,高要,四会,兴宁,陆丰,阳春,英德,连州,普宁,罗定\"},\"海南\":{\"地级\":\"海口,三亚\",\"县级\":\"琼海,文昌,万宁,五指山,儋州,东方\"},\"云南\":{\"地级\":\"昆明,曲靖,玉溪,保山,昭通,丽江,普洱,临沧\",\"县级\":\"安宁,宣威,个旧,开远,景洪,楚雄,大理,潞西,瑞丽\"},\"贵州\":{\"地级\":\"贵阳,六盘水,遵义,安顺\",\"县级\":\"清镇,赤水,仁怀,铜仁,毕节,兴义,凯里,都匀,福泉\"},\"四川\":{\"地级\":\"成都,绵阳,德阳,广元,自贡,攀枝花,乐山,南充,内江,遂宁,广安,泸州,达州,眉山,宜宾,雅安,资阳\",\"县级\":\"都江堰,彭州,邛崃,崇州,广汉,什邡,绵竹,江油,峨眉山,阆中,华蓥,万源,简阳,西昌\"},\"湖南\":{\"地级\":\"长沙,株洲,湘潭,衡阳,岳阳,郴州,永州,邵阳,怀化,常德,益阳,张家界,娄底\",\"县级\":\"浏阳,醴陵,湘乡,韶山,耒阳,常宁,武冈,临湘,汨罗,津市,沅江,资兴,洪江,冷水江,涟源,吉首\"},\"湖北\":{\"地级\":\"武汉,襄樊,宜昌,黄石,鄂州,随州,荆州,荆门,十堰,孝感,黄冈,咸宁\",\"县级\":\"大冶,丹江口,洪湖,石首,松滋,宜都,当阳,枝江,老河口,枣阳,宜城,钟祥,应城,安陆,汉川,麻城,武穴,赤壁,广水,仙桃,天门,潜江,恩施,利川\"},\"河南\":{\"地级\":\"郑州,洛阳,开封,漯河,安阳,新乡,周口,三门峡,焦作,平顶山,信阳,南阳,鹤壁,濮阳,许昌,商丘,驻马店\",\"县级\":\"巩义,新郑,新密,登封,荥阳,偃师,汝州,舞钢,林州,卫辉,辉县,沁阳,孟州,禹州,长葛,义马,灵宝,邓州,永城,项城,济源\"},\"山西\":{\"地级\":\"太原,大同,忻州,阳泉,长治,晋城,朔州,晋中,运城,临汾,吕梁\",\"县级\":\"古交,潞城,高平,介休,永济,河津,原平,侯马,霍州,孝义,汾阳\"},\"陕西\":{\"地级\":\"西安,咸阳,铜川,延安,宝鸡,渭南,汉中,安康,商洛,榆林\",\"县级\":\"兴平,韩城,华阴\"},\"甘肃\":{\"地级\":\"兰州,天水,平凉,酒泉,嘉峪关,金昌,白银,武威,张掖,庆阳,定西,陇南\",\"县级\":\"玉门,敦煌,临夏,合作\"},\"青海\":{\"地级\":\"西宁\",\"县级\":\"格尔木,德令哈\"},\"江西\":{\"地级\":\"南昌,九江,赣州,吉安,鹰潭,上饶,萍乡,景德镇,新余,宜春,抚州\",\"县级\":\"乐平,瑞昌,贵溪,瑞金,南康,井冈山,丰城,樟树,高安,德兴\"},\"台湾\":{\"地级\":\"台北,台中,基隆,高雄,台南,新竹,嘉义\",\"县级\":\"板桥,宜兰,竹北,桃园,苗栗,丰原,彰化,南投,太保,斗六,新营,凤山,屏东,台东,花莲,马公\"},\"香港\":{\"地级\":\"中西区,东区,九龙城区,观塘区,南区,深水埗区,黄大仙区,湾仔区,油尖旺区,离岛区,葵青区,北区,西贡区,沙田区,屯门区,大埔区,荃湾区,元朗区\"},\"澳门\":{\"地级\":\"花地玛堂区,圣安多尼堂区,花王堂区,望德堂区,大堂区,风顺堂区,圣老楞佐堂区,离岛,凼仔,路环\"}}";

    static  final String PROVINC_AREA =  "{\"山东\":\"华东\",\"江苏\":\"华东\",\"安徽\":\"华东\", \"浙江\":\"华东\",\"福建\":\"华东\",\"上海\":\"华东\",\"广东\":\"华南\",\"广西\":\"华南\",\"海南\":\"华南\",\"湖北\":\"华中\",\"湖南\":\"华中\",\"河南\":\"华中\",\"江西\":\"华中\",\"北京\":\"华北\",\"天津\":\"华北\",\"河北\":\"华北\",\"山西\":\"华北\",\"内蒙古\":\"华北\",\"宁夏\":\"西北\",\"新疆\":\"西北\",\"青海\":\"西北\",\"陕西\":\"西北\",\"甘肃\":\"西北\",\"四川\":\"西南\",\"云南\":\"西南\",\"贵州\":\"西南\",\"西藏\":\"西南\",\"重庆\":\"西南\",\"辽宁\":\"东北\",\"吉林\":\"东北\", \"黑龙江\":\"东北\",\"台湾\":\"台港澳\",\"香港\":\"台港澳\",\"澳门\":\"台港澳\"}";
    static Map<String,String> PRO_TO_AREA = new HashMap<String,String>();
    static String CHINA_PRO_CITYS ;

    static Pattern PA_CHINA_PROVINCE;
    static Pattern PA_CHINA_CITY;
    static {
        JSONObject a = new JSONObject(PROVINCE_CITYS);
        Set keys = a.keySet();
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        ArrayList<String> d = new ArrayList<String>();
        for(Object key:keys){
            String pro = (String) key;
            Object data = a.get(pro);

            if(data instanceof String){
                for(String x: ((String) data).split(",")){
                    if(x.matches(".*(区|县)$") && x.length() > 2)
                        d.add(x.substring(0,x.length() -1));
                    else
                        d.add(x);

                }
            }else if(data instanceof JSONObject){

                for(String x:((JSONObject) data).getString("地级").split(",")){
                    if(x.matches(".*(区|县)$") && x.length() > 2)
                        d.add(x.substring(0,x.length() -1));
                    else
                        d.add(x);
                }
                if(((JSONObject) data).has("县级")){
                    for(String x:((JSONObject) data).getString("县级").split(",")){
                        if(x.matches(".*(区|县)$") && x.length() > 2)
                            d.add(x.substring(0,x.length() -1));
                        else
                            d.add(x);
                    }
                }
            }

        }
        for(int i = 0;i < d.size();++i){
            builder.append(d.get(i));
            if(i != d.size() - 1)
                builder.append("|");
        }
        builder.append(")");
        CHINA_PRO_CITYS = builder.toString();
        PA_CHINA_CITY = Pattern.compile(CHINA_PRO_CITYS);
        PA_CHINA_PROVINCE = Pattern.compile(RE_CHINA_PROVINCE);
        {

            JSONObject z = new JSONObject(PROVINC_AREA);
            Set keys1 = z.keySet();
            for(Object key:keys1){
                PRO_TO_AREA.put((String)key,z.getString((String)key));
            }
        }
    }
    public static void main(String[] args){
        String a = "海淀区";
        if(a.matches(".*(区|县)$") && a.length() > 2){
            System.out.println(a.substring(0,a.length() - 1));
        }
    }

}
