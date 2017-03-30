<?php
include("db_con.php");
header('Content-type:application/json');
$token = $_REQUEST["token"];
$user_id = $_REQUEST["userid"];

$mysql_qry = "select user_id,token_body from tokens where user_id = ? and token_body=?";
// $result_token = mysqli_query($conn ,$token_qry);

$result_token = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result_token,'ss',$user_id,$token);
mysqli_stmt_execute($result_token);
$result_token= mysqli_stmt_get_result($result_token);

$row_token=mysqli_fetch_array($result_token);
$ret=array();
if(!$row_token){
    http_response_code(403);
    $ret['message']="You don't have permission to access";
    echo json_encode($ret);
    exit();
}

?>