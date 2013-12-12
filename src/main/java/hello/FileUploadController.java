package hello;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class FileUploadController {


    @RequestMapping(value="/error", method=RequestMethod.GET)
    public @ResponseBody String error(@RequestParam("message") String message){
    	return message;
    }

    @RequestMapping(value="/", method=RequestMethod.GET)
    public String index(){
    	return "redirect:/upload";
    }


    @RequestMapping(value="/download", method=RequestMethod.GET)
    public void download(@RequestParam("id") String id, HttpServletResponse response) throws IOException{

		SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select name, bytes from test where id=?" ,id);
		sqlRowSet.next();
		String name = sqlRowSet.getString(1);
		name = new String(name.getBytes("utf-8"),"iso-8859-1");
		byte[] b = (byte[])sqlRowSet.getObject(2);

		response.setHeader("Content-Disposition", "attachment; filename=\""+name+"\"");
		response.getOutputStream().write(b);
    }

    @RequestMapping(value="/upload", method=RequestMethod.GET)
    public String provideUploadInfo(@RequestParam(value="id",required=false) String id, Model model, RedirectAttributes attributes) {
    	if(id!=null){

    		attributes.addAttribute("id", id);

    		return "redirect:/download";

    	}else{
        	try{
            	jdbcTemplate.execute("create table test(id varchar(20), datetime varchar(20), comment varchar(100), name varchar(100), bytes binary)");
            	Logger.getLogger("").info("テーブルを作成しました。");
            }catch(Exception e){
            }
        	String[][] data = jdbcTemplate.query("select id, datetime, comment, name from test order by id desc", new ResultSetExtractor<String[][]>(){
        		@Override
        		public String[][] extractData(ResultSet rs) throws SQLException{
        			Vector<String[]> v = new Vector<String[]>();
        			while(rs.next()){
        				v.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
        			}
    				return v.toArray(new String[v.size()][]);
        		}
        	});
        	model.addAttribute("data", data);

            return "upload";
    	}
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public String handleFileUpload(@RequestParam("comment") String comment,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes attributes){
        if (!file.isEmpty()) {
            try {
            	comment = new String(comment.getBytes("iso-8859-1"),"utf-8");
            	String name = file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                jdbcTemplate.update("insert into test values(?,?,?,?,?)",  String.valueOf(System.currentTimeMillis()), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), comment, name, bytes);
//            	name = new String(name.getBytes("utf-8"),"iso-8859-1");
//            	return "You successfully uploaded " + name + " " + bytes.length + " bytes !";

                return "redirect:/upload";
            } catch (Exception e) {
            	String message = "You failed to upload => " + e.getMessage();
            	attributes.addAttribute("message", message);
            	return "redirect:/error";
            }
        } else {
        	String message = "You failed to upload because the file was empty.";
        	attributes.addAttribute("message", message);
        	return "redirect:/error";
        }
    }
}