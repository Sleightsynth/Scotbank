
<?php
    $conn = mysqli_connect("localhost", "root", '', "my_db");

    if (!$conn) {
    die("Connection Failed");
    }

    $email=$_POST['email'];
    $password=$_POST['password'];

    if ($email != '' and $password !='' ) {
    	$sql = "SELECT * FROM customerinfo WHERE email = '$email' AND password = '$password'";

    	$results = mysqli_query($conn, $sql);

    	$num = mysqli_num_rows($results);

    	if ($num!=0) {
    		$sql = "SELECT firstName, customerID FROM customerinfo WHERE email = '$email' AND password = '$password'";
    		$results = mysqli_query($conn, $sql);
    		while ($row = mysqli_fetch_array($results)) {
        		$firstName = $row['firstName'];
        		$customerID = $row['customerID'];

        		session_start();
        		$_SESSION['firstName'] = $firstName;
        		$_SESSION['customerID'] = $customerID;
        		include 'homePage.hbs';
    		}
    	}
    	else {
    		echo"Email and/or password are not reconigsed.";
    		include 'homePage.hbs';
        }
    mysqli_free_result($results);

    mysqli_close($conn);
    }
?>