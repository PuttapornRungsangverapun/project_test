<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');

//$user_id = $_REQUEST["userid"];
$user_name_friend = $_REQUEST["username"];
$mysql_qry = "select user_id,user_username from users where user_username = ? and user_status_id = '1'";
//$result = mysqli_query($conn ,$mysql_qry);
 
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$user_name_friend);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_array($result);
$ret = array();

if($row){
    $frined_id=$row['user_id'];
    
    if(!($frined_id==$user_id)){
        $mysql_qry = "insert into friends(user_id,friend_id) values (?,?)";
        //  mysqli_query($conn ,$mysql_qry);
        
        $result = mysqli_prepare($conn ,$mysql_qry);
        mysqli_stmt_bind_param($result,'ss',$user_id,$frined_id);
        mysqli_stmt_execute($result);
        $result= mysqli_stmt_get_result($result);
        
        $ret['status']="success";
        $ret['message']="Success";
    }
    else{
        $ret['status']="error";
        $ret['message']="Error";
    }
}
else{
    $ret['status']="error";
    $ret['message']=$user_name_friend." does not exist";
    //$ret['sql']=$mysql_qry;
}
echo json_encode($ret);
?>