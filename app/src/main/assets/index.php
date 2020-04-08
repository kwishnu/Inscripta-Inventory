<?php
require 'phpspreadsheet/vendor/autoload.php';
require 'vendor/autoload.php';
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;
use PhpOffice\PhpSpreadsheet\IOFactory;
$inputFileType = 'Xlsx';
$inputFileName = '../../PL_Inventory/InventoryXls.xlsx';

//$inputFileName = '../../Users\ken.wishart\Box\ResDev\SystemsIntegration\Tech Dev/InventoryXls.xlsx';
//C:\Users\ken.wishart\Box\ResDev\SystemsIntegration\Tech Dev
//$spreadsheet = IOFactory::load($_SERVER['DOCUMENT_ROOT'].'/path/file.xlsx');

if (isset($_GET["Reason"])){
    $spreadsheet = IOFactory::load($inputFileName);
    $writer = IOFactory::createWriter($spreadsheet, $inputFileType);

    $reason = $_GET['Reason'];
    $imageNum = $_GET['ImageNum'];
    $partNumber = $_GET['PartNumber'];
    $itemName = $_GET['ItemName'];
    $minStockLevel = $_GET['MinStockLevel'];
    $invCount = $_GET['InvCount'];
    $sheetNum = $_GET['Sheet'];
    $rowNum = $_GET['RowNum'];
    $sendWarning = $_GET['SendWarning'];

    //if part number matches, proceed...else respond with error message...todo

if ($reason == 'addItem'){
    $spreadsheet->getSheet($sheetNum - 1)->insertNewRowBefore(3,1);
    $spreadsheet->getSheet($sheetNum - 1)->getCell('A3')->setValue($sheetNum);
    $spreadsheet->getSheet($sheetNum - 1)->getCell('B3')->setValue($imageNum);
    $spreadsheet->getSheet($sheetNum - 1)->getCell('C3')->setValue($partNumber);
    $spreadsheet->getSheet($sheetNum - 1)->getCell('D3')->setValue($itemName);
    $spreadsheet->getSheet($sheetNum - 1)->getCell('E3')->setValue($minStockLevel);
    $spreadsheet->getSheet($sheetNum - 1)->getCell('F3')->setValue($invCount);

    $spreadsheet->getSheet($sheetNum - 1)->getStyle('A2:I2')->getFill()
    ->setFillType(\PhpOffice\PhpSpreadsheet\Style\Fill::FILL_SOLID)
    ->getStartColor()->setARGB('FFFFFFFF');

    $spreadsheet->getSheet($sheetNum - 1)->getStyle('A2:I2')->getFont()
    ->setBold(FALSE);

    $spreadsheet->getSheet($sheetNum - 1)->getStyle('A2:I2')
    ->getAlignment()->setHorizontal(\PhpOffice\PhpSpreadsheet\Style\Alignment::HORIZONTAL_CENTER);

    $spreadsheet->getSheet($sheetNum - 1)->getStyle('D2')
    ->getAlignment()->setHorizontal(\PhpOffice\PhpSpreadsheet\Style\Alignment::HORIZONTAL_LEFT);

}
if ($reason == 'editItem'){
    $imageNumCell = "B".$rowNum;
    $partNumCell = "C".$rowNum;
    $nameCell = "D".$rowNum;
    $minLevelCell = "E".$rowNum;
    $invCountCell = "F".$rowNum;
    $spreadsheet->getSheet($sheetNum - 1)->getCell($imageNumCell)->setValue($imageNum);
    $spreadsheet->getSheet($sheetNum - 1)->getCell($partNumCell)->setValue($partNumber);
    $spreadsheet->getSheet($sheetNum - 1)->getCell($nameCell)->setValue($itemName);
    $spreadsheet->getSheet($sheetNum - 1)->getCell($minLevelCell)->setValue($minStockLevel);
    $spreadsheet->getSheet($sheetNum - 1)->getCell($invCountCell)->setValue($invCount);
}
if ($reason == 'deleteItem'){
    $invCountCell = "F".$rowNum;
    $spreadsheet->getSheet($sheetNum - 1)->removeRow($rowNum);
}
if ($reason == 'changeCount'){
    $invCountCell = "F".$rowNum;
    $spreadsheet->getSheet($sheetNum - 1)->getCell($invCountCell)->setValue($invCount);
}
    $writer->save($inputFileName);

if ($sendWarning == 'true'){
    $mail = new PHPMailer(true);

    try {

        //Server settings
        $mail->SMTPDebug = SMTP::DEBUG_SERVER;                      // Enable verbose debug output
        $mail->isSMTP();                                            // Send using SMTP
        $mail->Host       = 'smtp.office365.com';                    // Set the SMTP server to send through
        $mail->SMTPAuth   = true;                                   // Enable SMTP authentication
        $mail->Username   = 'ken.wishart@inscripta.com';                     // SMTP username
        $mail->Password   = 'Sadiedog1!';                               // SMTP password
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;         // Enable TLS encryption; `PHPMailer::ENCRYPTION_SMTPS` encouraged
        $mail->Port       = 587;                                    // TCP port to connect to, use 465 for `PHPMailer::ENCRYPTION_SMTPS` above

        $mail->setFrom('ken.wishart@inscripta.com', 'Inscripta Inventory');
        //    $mail->addAddress('kwish1777@gmail.com', 'Ken Wishart');     // Add a recipient
        $mail->addAddress('ken.wishart@inscripta.com', 'Ken Wishart');     // Add a recipient
        //           $mail->addCC('cc@example.com');
        //    $mail->addBCC('bcc@example.com');
        // Content
        $mail->isHTML(true);                                  // Set email format to HTML
        $mail->Subject = 'Inventory Notice';
        $mail->Body    = "Attention:<br/>A critical count warning has been triggered for <b>".$itemName."</b>, Part No. <b><a style=\"text-decoration:none;\" href=\"#\"><font color=\"000000\">".$partNumber."</font></a></b>. The current inventory count is ".$invCount.", while the Minimum Stock Level has been set at ".$minStockLevel.".<br/><br/>PL1 Inventory App<br/>(No reply: Server-generated e-mail)<br/><br/><br/><br/>";
        $mail->AltBody = "Attention:
        A critical count warning has been triggered for ".$itemName.", Part No. ".$partNumber.". The current inventory count is ".$invCount.", while the Minimum Stock Level has been set at ".$minStockLevel."
        PL1 Inventory App
        (No reply: Server-generated e-mail)";

        $mail->send();
        //    echo 'Message has been sent';
    } catch (Exception $e) {
        //    echo "Message could not be sent. Mailer Error: {$mail->ErrorInfo}";
    }
}
    echo ("Success");

}else{//Send InventoryXls data

$reader = IOFactory::createReader($inputFileType);
$reader->setReadDataOnly(true);
$reader->setLoadAllSheets();
$spreadsheet = $reader->load($inputFileName);

$loadedSheets = $spreadsheet->getSheetNames();
$all = new ArrayObject();

foreach($loadedSheets as $sheetIndex => $loadedSheet){
    $sheet = $spreadsheet->getSheet($sheetIndex);
    $sheetData = $sheet->toArray(null, true, true, true);
    $all->append($sheetData);
}

echo json_encode($all);

}