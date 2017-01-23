<?php 
include("db_con.php");
//header('Content-type:application/json');
$user_name = "pp1";//$_REQUEST["username"];
$user_pass = sha1("12345678");//sha1($_REQUEST["password"]);
$mysql_qry = "select user_id,user_username,user_password from users where user_username = '$user_name'";

$result = mysqli_query($conn ,$mysql_qry);
echo $result;
$row=mysqli_fetch_array($result);

if($row){
if($row['user_password']==($user_pass)){
$ret['status']="success";
$ret['userid']=intval($row['user_id']);	
$ret['message']="Login success";
}
else{
$ret['status']="fail1";	
$ret['message']="Login fail";
}
}
else{
$ret['status']="fail2";	
$ret['message']="Login fail";
}
echo json_encode($ret);	
?>

